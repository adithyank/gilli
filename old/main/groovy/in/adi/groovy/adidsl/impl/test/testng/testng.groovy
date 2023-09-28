package in.adi.groovy.adidsl.impl.test.testng

import in.adi.groovy.adidsl.fw.DSLInit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

class testng_Init implements DSLInit
{
	@Override
	void compilerConfiguration(CompilerConfiguration compilerConfiguration, ImportCustomizer imports)
	{
		//imports.addStaticStars(TestNgDSL.name)
		imports.addImport("testng", TestNGSpec.name)
	}

	@Override
	void init()
	{

	}
}

class TestNgDSL
{
	public static List<TestNGSpec> specList = new ArrayList<>()

	static TestNGSpec test(String testname, Closure closure)
	{
		TestNGSpec spec = new TestNGSpec()
		spec.name(testname)
		closure.delegate = spec
		closure.resolveStrategy = Closure.DELEGATE_ONLY
		closure()

		spec.run()

		return spec
	}

	static void withFile(Class klass, String testname, String expectedContentFilename, Closure<String> closureReturningFileNameOfActualContent)
	{

	}
}

class TestNGSpec
{
	String name
	Object actual
	Object expected
	String msg

	boolean  hasMsg()
	{
		return msg != null && !msg.isEmpty()
	}

	void name(Object name)
	{
		this.name = name
	}

	void actual(Object actual)
	{
		this.actual = actual
	}

	void expected(Object expected)
	{
		this.expected = expected
	}

	void msg(Object msg)
	{
		this.msg = msg
	}

	@Override
	String toString()
	{
		return name
	}
}
