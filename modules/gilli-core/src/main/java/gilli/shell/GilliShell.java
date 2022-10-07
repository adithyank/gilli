package gilli.shell;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class GilliShell
{
	public static final String RES_DEFAULT_IMPORTS_PATH = "META-INF/gilli/default-imports.txt";
	public static final String RES_STATIC_STAR_IMPORTS_PATH = "META-INF/gilli/static-star-imports.txt";

	private CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

	private GroovyShell groovyShell;
	private Binding binding;

	public GilliShell()
	{
		this((Map<String, Object>) null);
	}

	public GilliShell(Map<String, ?> variables)
	{
		if (variables == null)
			binding = new Binding();
		else
			binding = new Binding(variables);

		addDefaultImports();

		this.groovyShell = new GroovyShell(binding, compilerConfiguration);
	}

	private List<String> lines(String resourceName)
	{
		try
		{
			List<String> list = new ArrayList<>();

			Enumeration<URL> ress = GilliShell.class.getClassLoader().getResources(resourceName);

			while (ress.hasMoreElements())
			{
				InputStream is = ress.nextElement().openStream();

				try (BufferedReader r = new BufferedReader(new InputStreamReader(is)))
				{
					r.lines().forEach(list::add);
				}
			}

			return list;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex.toString(), ex);
		}
	}

	private void addDefaultImports()
	{
		ImportCustomizer imports = new ImportCustomizer();
		compilerConfiguration.addCompilationCustomizers(imports);

		lines(RES_DEFAULT_IMPORTS_PATH).forEach(imports::addImports);

		lines(RES_STATIC_STAR_IMPORTS_PATH).forEach(imports::addStaticStars);
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
