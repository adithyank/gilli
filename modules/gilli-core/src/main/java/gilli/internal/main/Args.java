package gilli.internal.main;

import gilli.extras.dict.DictionaryCmdLine;
import gilli.extras.panchapakshi.PanchaPakshiCmd;
import gilli.files.FileCmd;
import gilli.flutter.FlutterDevCmd;
import gilli.pwd.PasswordStore;
import gilli.util.GeneralUtil;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandLine.Command(name = "gilli", sortOptions = false, synopsisHeading = "Gilli : A DSL Platform\n\n",
subcommands = {HelpCommand.class, PasswordStore.class, PanchaPakshiCmd.class, DictionaryCmdLine.class,
FileCmd.class, FlutterDevCmd.class})
public class Args implements Runnable
{
//    @Option(names = {"-h", "--help"}, description = "Print Help Message and Exit")
//    private boolean help = false;

    @Option(names = {"-v", "--version"}, description = "Print Gilli Version and Exit")
    private boolean version;

    //setter method defines the parameter
    private File[] files;

    @Option(names = {"-e", "--exec"}, description = "Script Text For Execution")
    private String scriptText;

    @Option(names = {"-p", "--print"}, description = "Execute the Script Text and the returned value is printed")
    private String scriptTextForPrint;

    @Option(names = {"-i", "--forEachStdinLine"}, description = "The given code will be executed (-e or -p) for each line in the standard input stream. The value of the each line will be available in the implicit 'line' variable which the script text can use for its purpose")
    private boolean forEachStdinLine = false;

    @Option(names = {"-x", "--complexScriptForEachLine"}, description = "If this option is given the value of '-e' or '-f' is understood as complex-closure of the form 'statement_1; statement_2;....statement_n; EACHLINE {....}; END {....}'. EACHLINE closure will be called for each line from the standard input stream. The value of each line will be available in the implicit 'line' variable which the script text in EACHLINE closure can use for its purpose. The END closure will be called only once at the end after all the input lines. With this option, you can implement awk like input text processing functionality")
    private boolean complexScriptForEachLine;

    @Option(names = {"-V", "--variable"}, description = "-V key=value syntax can be used to supply named variables from command line to the executed script. ex: " + Gilli.CMD + " -V country='India' -e 'println country.toUpperCase()'")
    private Map<String, String> variables;

    @Option(names = {"--stacktrace"}, description = "Prints stacktrace during Exception. default : ${DEFAULT-VALUE}")
    private boolean stacktrace = false;

    @Option(names = {"-k", "--keepRunning"}, description = "Makes the " + Gilli.PRODUCT + " keep running even after the execution is over. An option for daemon works. default : ${DEFAULT-VALUE}")
    private boolean keepRunning = false;

    @CommandLine.Spec
    CommandSpec spec; // injected by picocli

//    public boolean isHelp() {
//        return help;
//    }

    @Parameters(index = "0..*", description = "Gilli script files to execute")
    public void setFiles(File[] files)
    {
        List<File> notAvailableFiles = Arrays.stream(files).filter(f -> !f.exists()).collect(Collectors.toList());

        if (!GeneralUtil.isEmpty(notAvailableFiles))
            throw new CommandLine.ParameterException(spec.commandLine(), "Script File(s) not found: " + notAvailableFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList()));

        List<File> errorFiles = Arrays.stream(files).filter(Gilli::invalidScriptExtension).collect(Collectors.toList());

        if (!GeneralUtil.isEmpty(errorFiles))
            throw new CommandLine.ParameterException(spec.commandLine(), "Script file extension is not 'gilli' for files: " + errorFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList()));

        this.files = files;
    }

    public boolean hasFilesToExecute()
    {
        return files != null && files.length > 0;
    }

    public File[] getFiles() {
        return files;
    }

    public boolean isVersion() {
        return version;
    }

    public boolean hasScriptTextToExecute()
    {
        return GeneralUtil.hasValue(scriptText);
    }

    public boolean hasScriptTextToExecuteWithoutStdinReading()
    {
        return hasScriptTextToExecute() && doesNotHaveStdinReading();
    }

    public boolean doesNotHaveStdinReading()
    {
        return !shouldWorkForEachStdinLine() && !hasComplexClosureOption();
    }

    public String getScriptText() {
        return scriptText;
    }

    public boolean hasComplexClosureOption() {
        return complexScriptForEachLine;
    }

    public boolean hasStacktraceOption() {
        return stacktrace;
    }

    public boolean hasPrintOption()
    {
        return GeneralUtil.hasValue(scriptTextForPrint);
    }

    public boolean hasPrintOptionWithoutStdinReading()
    {
        return hasPrintOption() && doesNotHaveStdinReading();
    }

    public String getScriptTextForPrint() {
        return scriptTextForPrint;
    }

    public boolean shouldWorkForEachStdinLine()
    {
        return forEachStdinLine;
    }

    public boolean shouldKeepRunning()
    {
        return keepRunning;
    }

    public Map<String, String> getVariables()
    {
        return variables;
    }

    @Override
    public void run()
    {
        GilliMain.instance.exec();
    }
}
