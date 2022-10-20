package gilli.internal.main;

import groovy.lang.Closure;
import groovy.util.Eval;
import gilli.shell.GilliShell;
import gilli.util.GeneralUtil;
import gilli.util.Stdin;
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

public class GilliMain
{
    public static final GilliMain instance = new GilliMain();

    private final Map<String, String> scriptConf = new HashMap<>();
    private final Args parsedArgs = new Args();

    public final CommandLine cmdLine = new CommandLine(parsedArgs);

    private GilliShell defaultShell;

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

            /*
               This if condition is added to run the `gilli` methods from within the IDE. In the absense of this,
               Exceptions would come due to missing files, properties, etc
             */
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
            return System.getProperty(Gilli.SYSPROP_PREFIX + "." + suffix);
        }
    }

    /**
     * TODO : May create issues if this method is called concurrently... :(
     * To think whether this method will be called concurrently as it is in Main class...
     *
     * @return
     */
    public GilliShell shell(Map<String, ?> variables)
    {
        if (defaultShell == null)
            defaultShell = new GilliShell(variables);

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
            System.err.println(ConsoleInputs.SCRIPT_CONF + "not found or corrupted");
        }
    }

    public void exec()
    {
//        if (parsedArgs.isHelp())
//        {
//            cmdLine.usage(System.out);
//            Gilli.exitNormally();
//        }
        if (parsedArgs.isVersion())
        {
            doVersionWork();
            Gilli.exitNormally();
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
        shell(parsedArgs.getVariables());

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
                Gilli.exitAbnormally();
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
                //TODO : yet to implement : https://github.com/adithyank/gilli/issues/5
                state = ExecState.EXECUTED_AND_NORMAL;
            }
        }

        if (state == ExecState.INVALID_INPUTS)
        {
            cmdLine.usage(System.out);
            Gilli.exitAbnormally();
        }

        //System.out.println("keep = " + parsedArgs.shouldKeepRunning());

        if (parsedArgs.shouldKeepRunning())
        {
            LiveThread.takeoff();
        }
        else
        {
            if (state == ExecState.EXECUTED_AND_NORMAL)
                Gilli.exitNormally();
            else //if (state == ExecState.EXECUTED_AND_EXCEPTION)
                Gilli.exitAbnormally();
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
        Stream<String> resourceAsLines = Gilli.getResourceAsLines("gilli-version.info");

        List<String> currentVersion = resourceAsLines.collect(Collectors.toList());

        currentVersion.forEach(System.out::println);

        System.out.println("JVM Used For this Execution : " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        System.out.println();
    }

    public static void main(String[] args)
    {
        //System.out.println("Gilli is the top-most forked and contributed java project in github. SURE");

        //args = new String[] {"-p", "2/33", "--keepRunning"};

        //instance.init(args);
        //instance.exec();
        instance.cmdLine.setUsageHelpWidth((int) (ConsoleInputs.CONSOLE_COLS * 0.6f));
        instance.fillScriptConf();
        instance.cmdLine.execute(args);
    }
}
