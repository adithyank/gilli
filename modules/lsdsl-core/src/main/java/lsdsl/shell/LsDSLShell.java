package lsdsl.shell;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class LsDSLShell
{
	private CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

	private GroovyShell groovyShell;
	private Binding binding;

	public LsDSLShell()
	{
		this((Map<String, Object>) null);
	}

	public LsDSLShell(File parentDirectoryOfInitConfigFiles) throws Exception
	{
		this((Map<String, Object>) null);
	}

	public LsDSLShell(Map<String, Object> variables)
	{
		if (variables == null)
		{
			binding = new Binding();
		}
		else
		{
			binding = new Binding(variables);
		}
		
		this.groovyShell = new GroovyShell(binding, compilerConfiguration);
	}

	private void dslInit()
	{
		long t0 = System.currentTimeMillis();
		
		ImportCustomizer imports = new ImportCustomizer();

		compilerConfiguration.addCompilationCustomizers(imports);

		long t1 = System.currentTimeMillis();
		
		long t2 = System.currentTimeMillis();
	}

	public Object runScriptFile(File file) throws IOException
	{
		return groovyShell.evaluate(file);
	}
	
	public Object runScriptURI(URI uri) throws IOException
	{
		return groovyShell.evaluate(uri);
	}
	
	public Object runScriptText(String scriptText)
	{
		return groovyShell.evaluate(scriptText);
	}
	
}
