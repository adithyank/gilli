package gilli.util;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.Script;

import java.util.Map;

public class DslUtil
{
    public static <T> T assignDelegate(Closure closure, T delegate)
    {
        closure.setDelegate(delegate);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);

        return delegate;
    }

    public static <D, R> ClosureCall<R, D> call(Closure<R> closure, D delegate, Object... closureArgs)
    {
        closure = (Closure<R>) closure.clone();
        assignDelegate(closure, delegate);
        return new ClosureCall<R, D>(closure.call(closureArgs), delegate, closureArgs);
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

    public static class ClosureCall<R, D>
    {
        public Object[] args;
        public R returned;
        public D delegate;

        public ClosureCall(R returned, D delegate, Object... args)
        {
            this.returned = returned;
            this.delegate = delegate;
            this.args = args;
        }
    }

}
