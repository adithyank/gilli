package in.adi.groovy.adidsl.fw;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import in.adi.groovy.adidsl.fw.util.Util;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DSL
{
	private static long classInitializationStartTime = System.currentTimeMillis();
	
	private static final String URL_LATEST_VERSION_FILE = "http://cm.nmsworks.co.in:8081/artifactory/libs-release-local/in/co/nmsworks/cygnet/cygdsl-ga-VersionFile/latest/cygdsl-ga-VersionFile-latest.info";
	private static final String URL_LATEST_ZIP = "http://cm.nmsworks.co.in:8081/artifactory/libs-release-local/in/co/nmsworks/cygnet/cygdsl-ga/latest/cygdsl-ga-latest.zip";
	
	private static final String OPTION_VALUE_VERSION_UPDATE = "update";
	
	private static Logger logger = LogManager.getLogger();
	
	private static long cygdslInvocationTime;
	private static long argParsingStartTime;
	private static long argParsingEndTime;
	
	private static OptionParser PARSER;
	private static OptionSet GIVEN_OPTIONS;

	private static ArgumentAcceptingOptionSpec<String> HELP_OPTION;
	private static ArgumentAcceptingOptionSpec<String> FILE_OPTION;
	private static ArgumentAcceptingOptionSpec<String> TEXT_OPTION;
	private static ArgumentAcceptingOptionSpec<String> ARG_OPTION;
	private static ArgumentAcceptingOptionSpec<String> STACKTRACE_OPTION;
	private static ArgumentAcceptingOptionSpec<String> ALL_RAW_ARGS_OPTION;
	private static ArgumentAcceptingOptionSpec<String> INPUTSTREAM_OPTION;
	private static ArgumentAcceptingOptionSpec<String> LOGLEVEL_OPTION;
	private static ArgumentAcceptingOptionSpec<String> VERSION_OPTION;
	
	private static void parseArgs(String[] args)
	{
		argParsingStartTime = System.currentTimeMillis();
		
		PARSER = new OptionParser();

		HELP_OPTION = PARSER.acceptsAll(Arrays.asList("h", "help"), "Print Help Message").withOptionalArg();
		FILE_OPTION = PARSER.acceptsAll(Arrays.asList("f", "scriptFile"), "Script file to execute").withRequiredArg();
		TEXT_OPTION = PARSER.acceptsAll(Arrays.asList("e", "scriptText"), "Script Text to execute").withRequiredArg();
		ARG_OPTION = PARSER.acceptsAll(Arrays.asList("a", "arg"), "Argument to the script. Format : '-aCREATEDATE=2017-12-25'. Any number of this input can be given. The script can use the keys here as Direct Variables").withOptionalArg();
		STACKTRACE_OPTION = PARSER.acceptsAll(Arrays.asList("stacktrace"), "Prints stacktrace in case of any Exception during script Execution. If omitted, prints only the Exception message").withOptionalArg();
		ALL_RAW_ARGS_OPTION = PARSER.acceptsAll(Arrays.asList("allrawargs"), "Prints all raw args supplied").withOptionalArg();
		INPUTSTREAM_OPTION = PARSER.acceptsAll(Arrays.asList("in"), "The given code is executed for each line from the standard input stream. Each line will be available in the implicit 'line' variable that the script can use").withRequiredArg();
		LOGLEVEL_OPTION = PARSER.acceptsAll(Arrays.asList("loglevel"), "Log level. Valid values 'fatal|error|warn|info|debug|trace'").withRequiredArg().defaultsTo("error");
		VERSION_OPTION = PARSER.acceptsAll(Arrays.asList("v", "version"), "Prints cygdsl version. If value 'update' is given, the latest version will be downloaded and updated automatically").withOptionalArg();

		String colString = System.getProperty("COLS");
		int cols = (colString == null || colString.isEmpty()) ? 200 : Integer.parseInt(colString);


		PARSER.formatHelpWith(new BuiltinHelpFormatter(cols, 5));

		try
		{
			GIVEN_OPTIONS = PARSER.parse(args);
		}
		catch (OptionException uroex)
		{
			System.err.println(uroex.getMessage());
			System.exit(1);
		}

		argParsingEndTime = System.currentTimeMillis();
	}
	
	private static void printHelpAndExit() throws Exception
	{
		System.err.println();
		PARSER.printHelpOn(System.err);
		System.err.println();
		System.exit(0);
	}

	/**
	 * This is not a good approach. Yet to find a better one
	 * @throws Exception
	 */
	private static void initLogger() throws Exception
	{
		long s = System.currentTimeMillis();
		
		String logLevel = GIVEN_OPTIONS.valueOf(LOGLEVEL_OPTION);

		Configurator.setRootLevel(Level.valueOf(logLevel));

		logger.info("Logger Initialized");
		
		if (logger.isTraceEnabled())
		{
			logger.trace("Logger Initialized");
			logger.debug("Logger Initialized");
			logger.warn("Logger Initialized");
			logger.info("Logger Initialized");
			logger.error("Logger Initialized");
			logger.fatal("Logger Initialized");
		}
		
		long e = System.currentTimeMillis();

		logger.debug("Time taken by JVM for initialization = {} ms", classInitializationStartTime - cygdslInvocationTime);
		logger.debug("Time taken for before arguments-parsing = {} ms", argParsingStartTime - classInitializationStartTime);
		logger.debug("Time taken for arguments-parsing = {} ms", argParsingEndTime - argParsingStartTime);
		logger.debug("Time taken for Logger init = {} ms", e - s);
	}
	
	private static void run(String[] args) throws Exception
	{
		parseArgs(args);
		
		initLogger();
		
		long ps = System.currentTimeMillis();
		
		if (!GIVEN_OPTIONS.hasOptions() || GIVEN_OPTIONS.has(HELP_OPTION))
		{
			System.err.println("Script file/content not given !");
			printHelpAndExit();
		}
		
		if (GIVEN_OPTIONS.has(VERSION_OPTION))
		{
			File file = Util.fileFromHome("version.info");
			
			List<String> currentVersion = Files.readAllLines(file.toPath());

			System.out.println();

			System.out.println("Current Version");
			System.out.println("---------------");
			currentVersion.forEach(System.out::println);
			
			System.out.println("JVM : " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
			System.out.println();
			
			try
			{
				URL url = new URL(URL_LATEST_VERSION_FILE);

				List<String> newVersion = new BufferedReader(new InputStreamReader(url.openStream())).lines().collect(Collectors.toList());

				if (currentVersion.get(0).equals(newVersion.get(0)))
				{
					System.out.println("You are already in latest version");
				}
				else
				{
					String versionValue = GIVEN_OPTIONS.valueOf(VERSION_OPTION);
					
					if (versionValue != null && versionValue.equals(OPTION_VALUE_VERSION_UPDATE))
					{
						System.out.println("Downloading Latest version...!");
						
						URL download = new URL(URL_LATEST_ZIP);
						ReadableByteChannel from = Channels.newChannel(download.openStream());
						FileOutputStream oos = new FileOutputStream(Util.fileFromHome("cygdsl-ga-latest.zip"));
						oos.getChannel().transferFrom(from,0, Long.MAX_VALUE);

						System.out.println("Latest version downloaded");
					}
					else
					{
						System.out.println("=========================================");
						System.out.println("New Latest Version Available for Download");
						System.out.println("-----------------------------------------");

						newVersion.forEach(System.out::println);
						System.out.println("-----------------------------------------");
						System.out.println("Use the below URL For downloading the latest version");
						System.out.println(URL_LATEST_ZIP);
						System.out.println();
					}
				}
			}
			catch (Exception ex)
			{
				logger.error("New Version Check failed !");
				logger.trace("", ex);
			}
			
			System.exit(0);
		}

		if (GIVEN_OPTIONS.has(ALL_RAW_ARGS_OPTION))
		{
			System.out.println("Raw Args length = " + args.length);
			System.out.println();
			
			for (int i = 0; i < args.length; i++)
				System.out.println("Arg[" + i + "] = " + args[i]);
			
			System.exit(0);
		}
		
		if (!GIVEN_OPTIONS.has(FILE_OPTION) && !GIVEN_OPTIONS.has(TEXT_OPTION) && !GIVEN_OPTIONS.has(INPUTSTREAM_OPTION))
		{
			System.err.println("Script file/content not given");
			printHelpAndExit();
		}

		long pe = System.currentTimeMillis();
		
		logger.debug("Time taken for args-pre-check = {} ms", pe - ps);

		long is = System.currentTimeMillis();
		
		CompilerConfiguration cc = new CompilerConfiguration();
		
		dslInit(cc);

		long ie = System.currentTimeMillis();
		logger.debug("Time taken for DSL Inits = {} ms", ie - is);
		
		execute(cc);
	}
	
	private static void execute(CompilerConfiguration cc) throws Exception
	{
		long stime = System.currentTimeMillis();
		
		Binding binding = new Binding();

		GroovyShell shell = new GroovyShell(binding, cc);

		binding.setVariable("args", GIVEN_OPTIONS.nonOptionArguments());
		
		if (GIVEN_OPTIONS.has(ARG_OPTION))
		{
			List<String> keys = GIVEN_OPTIONS.valuesOf(ARG_OPTION);

			keys.stream()
					.filter(s -> s != null && !s.isEmpty())
					.map(s -> s.split("="))
					.forEach(kv ->
					{
						String k = kv[0].trim();
						
						String v = null;
						
						if (kv.length > 1)
							v = kv[1].trim();

						binding.setVariable(k, v);
					});
		}
		
		Callable callable = null;
		
		if (GIVEN_OPTIONS.has(FILE_OPTION))
		{
			File scriptFile = new File(GIVEN_OPTIONS.valueOf(FILE_OPTION));

			callable = () -> shell.evaluate(scriptFile);
		}
		else if (GIVEN_OPTIONS.has(TEXT_OPTION))
		{
			String scriptText = GIVEN_OPTIONS.valueOf(TEXT_OPTION);
			
			callable = () -> shell.evaluate(scriptText);
		}
		else if (GIVEN_OPTIONS.has(INPUTSTREAM_OPTION))
		{
			String scriptText = GIVEN_OPTIONS.valueOf(INPUTSTREAM_OPTION);

			String finalScriptText = "System.in.eachLine {line ->" + scriptText + " }";
			
			callable = () -> shell.evaluate(finalScriptText);
		}

		long etime = System.currentTimeMillis();
		
		logger.debug("Time taken for DSL Execution Preparation = {} ms", etime - stime);
		
		eval(callable);
	}
	
	private static void eval(Callable callable)
	{
		long s = System.currentTimeMillis();
		
		try
		{
			callable.call();
		}
		catch (Throwable th)
		{
			if (GIVEN_OPTIONS.has(STACKTRACE_OPTION))
				th.printStackTrace();
			else
				System.err.println(th.toString());
		}
		finally
		{
			long e = System.currentTimeMillis();
			logger.debug("Time taken for DSL Execution = {} ms", e - s);
			logger.debug("Total time Excluding JVM Init Time = {} ms", e - classInitializationStartTime);
			logger.debug("Total time = {} ms", e - cygdslInvocationTime);
		}
	}
	
	private static void dslInit(CompilerConfiguration cc)
	{
		ImportCustomizer imports = new ImportCustomizer();
		
		cc.addCompilationCustomizers(imports);
		
		File[] files = Util.fileFromHome("conf/init").listFiles();

		//System.out.println("files = " + Arrays.toString(files));
		
		if (files == null)
			return;
		
		Arrays.stream(files).forEach(file -> 
		{
			try
			{
				Predicate<String> predicate = line -> line != null && !line.trim().isEmpty() && !line.startsWith("#");
				
				Files.readAllLines(file.toPath()).stream().filter(predicate).forEach(className ->
				{
					try
					{
						DSLInit init = (DSLInit) Class.forName(className).newInstance();
		
						init.compilerConfiguration(cc, imports);
						init.init();
					}
					catch (Exception e)
					{
						System.err.println("Internal Error in Initing DSL : file = " + file + ", class = " + className);
						e.printStackTrace();
					}
				});
			}
			catch (IOException e)
			{
				System.err.println("Internal Error in Initing DSL : file = " + file);
				e.printStackTrace();
			}
		});
	}

	public static void main(String[] args) throws Exception
	{
		cygdslInvocationTime = Long.getLong("START_MILLIS");
		run(args);
	}
}
	