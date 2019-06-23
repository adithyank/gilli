package lsdsl.internal.main;

import groovy.lang.Closure;
import groovy.util.Eval;
import lsdsl.shell.LsDSLShell;
import lsdsl.util.GeneralUtil;
import lsdsl.util.Stdin;
import picocli.CommandLine;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
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
        public static int    CONSOLE_COLS;
        public static long   INVOCATION_TIME;
        public static String HOME;
        public static String INVOCATION_DIR;
        public static String SCRIPT_CONF;

        static
        {
            init();
        }

        private static void init()
        {
            String startMillisStr = sysProp("startmillis");

            if (GeneralUtil.hasValue(startMillisStr))
            {
                INVOCATION_TIME = Long.parseLong(startMillisStr);
                CONSOLE_COLS    = Integer.parseInt(sysProp("console.cols"));
                HOME            = sysProp("home");
                INVOCATION_DIR  = sysProp("invocation.dir");
                SCRIPT_CONF     = sysProp("script.conf");
            }
            else
            {
                INVOCATION_TIME = System.currentTimeMillis();
                CONSOLE_COLS = 100;
                HOME = System.getProperty("user.dir");
                INVOCATION_DIR = System.getProperty("user.dir");
            }
        }

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

        if (!GeneralUtil.hasValue(ConsoleInputs.SCRIPT_CONF))
            return;

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

    /**
     * Input Args validation flow and execution are done in same flow here. Otherwise, the same flow will have to be
     * done twice once for validation and another for execution.
     */
    private void execute()
    {
        shell();

        ExecState state = ExecState.INVALID_INPUTS;

        if (parsedArgs.hasFileToExecute())
        {
            state = run(() -> defaultShell.runScriptFile(parsedArgs.getFile()));
        }

        if (parsedArgs.hasScriptTextToExecuteWithoutStdinReading())
        {
            state = run(() -> defaultShell.runScriptText(parsedArgs.getScriptText()));
        }

        if (parsedArgs.hasPrintOptionWithoutStdinReading())
        {
            String scriptText = "println (" + parsedArgs.getScriptTextForPrint() + ')';
            state = run(() -> defaultShell.runScriptText(scriptText));
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

            state = run(() -> defaultShell.runScriptText(scriptText));
        }

        if (parsedArgs.hasComplexClosureOption())
        {
            if (parsedArgs.hasScriptTextToExecute())
            {
                String scriptText = "{it ->" + parsedArgs.getScriptText() + "}";

                Closure c = (Closure) Eval.me(scriptText);

                state = run(() -> Stdin.execLikeAwk(c));
            }
            else if (parsedArgs.hasFileToExecute())
            {
                //TODO : yet to implement : https://github.com/adithyank/lsdsl-core/issues/5
                state = ExecState.EXECUTED_AND_NORMAL;
            }
        }

        if (state == ExecState.INVALID_INPUTS)
        {
            cmdLine.usage(System.out);
            LsDSL.exitAbnormally();
        }

        //System.out.println("keep = " + parsedArgs.shouldKeepRunning());

        if (parsedArgs.shouldKeepRunning())
        {
            LiveThread.takeoff();
        }
        else
        {
            if (state == ExecState.EXECUTED_AND_NORMAL)
                LsDSL.exitNormally();
            else //if (state == ExecState.EXECUTED_AND_EXCEPTION)
                LsDSL.exitAbnormally();
        }
    }

    private static class LiveThread extends Thread
    {
        private LiveThread()
        {
        }

        public static void takeoff()
        {
            new LiveThread().start();
        }

        @Override
        public void run()
        {
            while (true)
            {
                GeneralUtil.sleep(1, TimeUnit.HOURS);
            }
        }
    }

    private ExecState run(Callable callable)
    {
        long s = System.currentTimeMillis();

        try
        {
            callable.call();
            return ExecState.EXECUTED_AND_NORMAL;
        }
        catch (Throwable th)
        {
            if (parsedArgs.hasStacktraceOption())
                th.printStackTrace();
            else
                System.err.println(th.toString());

            return ExecState.EXECUTED_AND_EXCEPTION;
        }
    }

    enum ExecState
    {
        EXECUTED_AND_NORMAL,
        EXECUTED_AND_EXCEPTION,
        INVALID_INPUTS
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

        //args = new String[] {"-p", "2/33", "--keepRunning"};

        instance.init(args);

        instance.exec();
    }
}
