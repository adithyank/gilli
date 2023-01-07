package gilli.util;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.Script;

import java.util.Map;

public class DSLHelper
{
    public static <T> T assignDelegate(Closure closure, T delegate)
    {
        closure.setDelegate(delegate);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);

        return delegate;
    }

    public static <D, R> R callAndGetReturned(Closure<R> closure, D delegate, Object... closureArgs)
    {
        assignDelegate(closure, delegate);
        return (R) closure.call(closureArgs);
    }

    public static <D, R> R callClonedClosureAndGetReturned(Closure<R> closure, D delegate, Object... closureArgs)
    {
    	return callAndGetReturned((Closure<R>) closure.clone(), delegate, closureArgs);
    }

    public static <D> D callAndGetDelegate(Closure closure, D delegate, Object... closureArgs)
    {
        assignDelegate(closure, delegate);
        closure.call(closureArgs);
        return delegate;
    }

    public static <D> D callClonedClosureAndGetDelegate(Closure closure, D delegate, Object... closureArgs)
    {
    	return callAndGetDelegate((Closure) closure.clone(), delegate, closureArgs);
    }

    public static <D> void call(Closure closure, D delegate, Object... closureArgs)
    {
        callAndGetReturned(closure, delegate, closureArgs);
    }

    @Deprecated
    public static <T> T assignDelegateAndCall(Closure closure, T delegate, Object... closureArgs)
    {
        closure.setDelegate(delegate);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);

        closure.call(closureArgs);

        return delegate;
    }

    public static <T> T runNewInstance(String className) throws Exception
    {
        return runNewInstance(className, null);
    }

    public static <T> T runNewInstance(String className, Map<String, Object> variables) throws Exception
    {
        Script script = (Script) Class.forName(className).newInstance();

        if (variables != null && !variables.isEmpty())
        {
            Binding binding = new Binding(variables);
            script.setBinding(binding);
        }

        return (T) script.run();
    }

    public static <T> T obj(Class<T> klass, Closure closure)
    {
        try
        {
            T o = klass.newInstance();

            call(closure, o);

            return o;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T assignDelegateOnly(Closure closure, T delegate)
    {
        closure.setDelegate(delegate);
        closure.setResolveStrategy(Closure.DELEGATE_ONLY);

        return delegate;
    }

    public static <D> D callAndGetDelegateOnly(Closure closure, D delegate, Object... closureArgs)
    {
        assignDelegateOnly(closure, delegate);
        closure.call(closureArgs);
        return delegate;
    }

    public static <D> D callClonedClosureAndGetDelegateOnly(Closure closure, D delegate, Object... closureArgs)
    {
		return callAndGetDelegateOnly((Closure) closure.clone(), delegate, closureArgs);
    }

}
