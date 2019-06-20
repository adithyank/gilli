package lsdsl.internal.main;

import groovy.lang.Closure;
import groovy.util.Eval;
import lsdsl.shell.LsDSLShell;
import lsdsl.util.Stdin;
import picocli.CommandLine;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LsDSLMain
{
    public static final LsDSLMain instance = new LsDSLMain();

    private final Map<String, String> scriptConf = new HashMap<>();
    private CommandLine cmdLine;
    private Args parsedArgs = new Args();

    private LsDSLShell defaultShell;

    static class ConsoleInputs
    {
        public static final int    CONSOLE_COLS    = Integer.parseInt(sysProp("console.cols"));
        public static final long   INVOCATION_TIME = Long.parseLong(sysProp("startmillis"));
        public static final String HOME            = sysProp("home");
        public static final String INVOCATION_DIR  = sysProp("invocation.dir");
        public static final String SCRIPT_CONF     = sysProp("script.conf");

        private static String sysProp(String suffix)
        {
            return System.getProperty(LsDSL.SYSPROP_PREFIX + "." + suffix);
        }
    }

    private void init(String[] args)
    {
        parseArgs(args);
        fillScriptConf();
    }

    private void parseArgs(String[] args)
    {
        cmdLine = new CommandLine(parsedArgs);

        cmdLine.setUsageHelpWidth((int) (ConsoleInputs.CONSOLE_COLS * 0.6f));

        try
        {
            cmdLine.parse(args);
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            System.err.println();
            cmdLine.usage(System.err);
            LsDSL.exitAbnormally();
        }
    }

    /**
     * TODO : May create issues if this method is called concurrently... :(
     * To think whether this method will be called concurrently as it is in Main class...
     *
     * @return
     */
    public LsDSLShell shell()
    {
        if (defaultShell == null)
            defaultShell = new LsDSLShell();

        return defaultShell;
    }

    private void fillScriptConf()
    {
        Properties p = new Properties();

        try (FileReader fr = new FileReader(ConsoleInputs.SCRIPT_CONF))
        {
            p.load(fr);
            p.forEach((k, v) -> scriptConf.put((String) k, (String) v));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.err.println("lsdsl-script.conf not found or corrupted");
        }
    }

    private void exec()
    {
        if (parsedArgs.isHelp())
        {
            cmdLine.usage(System.out);
            LsDSL.exitNormally();
        }
        else if (parsedArgs.isVersion())
        {
            doVersionWork();
            LsDSL.exitNormally();
        }
        else
        {
            execute();
        }
    }

    private void execute()
    {
        shell();

        if (parsedArgs.hasFileToExecute())
        {
            runAndExit(() -> defaultShell.runScriptFile(parsedArgs.getFile()));
        }

        if (parsedArgs.hasScriptTextToExecuteWithoutStdinReading())
        {
            runAndExit(() -> defaultShell.runScriptText(parsedArgs.getScriptText()));
        }

        if (parsedArgs.hasPrintOptionWithoutStdinReading())
        {
            String scriptText = "println (" + parsedArgs.getScriptTextForPrint() + ')';
            runAndExit(() -> defaultShell.runScriptText(scriptText));
        }

        if (parsedArgs.shouldWorkForEachStdinLine())
        {
            String scriptText;

            if (parsedArgs.hasPrintOption())
                scriptText = "System.in.eachLine {line -> println(" + parsedArgs.getScriptTextForPrint() + ") }";
            else if (parsedArgs.hasScriptTextToExecute())
                scriptText = "System.in.eachLine {line ->" + parsedArgs.getScriptText() + " }";
            else
            {
                System.err.println("Insufficient arguments");
                cmdLine.usage(System.out);
                LsDSL.exitAbnormally();
                scriptText = null; // just to satisfy java compiler. Otherwise, next line would show error
            }

            runAndExit(() -> defaultShell.runScriptText(scriptText));
        }

        if (parsedArgs.hasComplexClosureOption())
        {
            if (parsedArgs.hasScriptTextToExecute())
            {
                String scriptText = "{it ->" + parsedArgs.getScriptText() + "}";

                Closure c = (Closure) Eval.me(scriptText);

                runAndExit(() -> Stdin.execLikeAwk(c));
            }
            else if (parsedArgs.hasFileToExecute())
            {
                //TODO : yet to implement : https://github.com/adithyank/lsdsl-core/issues/5
            }
        }

        runAndExit(() -> {cmdLine.usage(System.out); return null;});
    }

    private void runAndExit(Callable callable)
    {
        long s = System.currentTimeMillis();

        try
        {
            callable.call();
        }
        catch (Throwable th)
        {
            if (parsedArgs.hasStacktraceOption())
                th.printStackTrace();
            else
                System.err.println(th.toString());

            LsDSL.exitAbnormally();
        }
        finally
        {
            LsDSL.exitNormally();
        }
    }


    private void doVersionWork()
    {
        Stream<String> resourceAsLines = LsDSL.getResourceAsLines("lsdsl-version.info");

        List<String> currentVersion = resourceAsLines.collect(Collectors.toList());

        currentVersion.forEach(System.out::println);

        System.out.println("JVM Used For this Execution : " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        System.out.println();
    }

    public static void main(String[] args)
    {
        //System.out.println("LsDSL is the top-most forked and contributed java project in github. SURE");

        instance.init(args);

        instance.exec();
    }
}
