package gilli.files.linesdiff;

import gilli.internal.main.Gilli;
import picocli.CommandLine;

@CommandLine.Command(name = "linesetdiff", sortOptions = false, synopsisHeading = "Line Diff as Set of Lines. So, lines order does not matter\n\n", description = "Line Diff as Set of Lines. So, lines order does not matter")
class FileLinesSetDiff implements Runnable
{
    private File firstFile;

    private File secondFile;

    @CommandLine.Option(negatable = true, names = ["-d"], description = "By default Lines only in first file and lines only in second file will be printed. Use this option for not printing the diff", defaultValue = "true")
    private boolean printDiff;

    @CommandLine.Option(names = ["-c", "--printCommon"], description = "Lines available in both the files will be printed", defaultValue = "false")
    private boolean printCommonLines;

    @CommandLine.Option(names = ["-t", "--trim"], description = "Trim Lines before comparison", defaultValue = "false")
    private boolean trim;

    @CommandLine.Option(names = ["-e", "--valueExtractionClosure"], description = "Closure Code for extracting the value from the file lines. NOT SUPPORTED NOW. WILL BE SUPPORTED LATER", defaultValue = "false")
    private String lineExtractionClosureCode;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    void ensureFile(File file)
    {
        if (!file.exists())
            throw new CommandLine.ParameterException(spec.commandLine(), "File [" + file + "] does not exist");
    }

    File getFirstFile()
    {
        return firstFile;
    }

    @CommandLine.Option(required = true, names = ["-1", "--first"], description = "First file path")
    void setFirstFile(File firstFile)
    {
        ensureFile(firstFile);
        this.firstFile = firstFile;
    }

    File getSecondFile()
    {
        return secondFile;
    }

    @CommandLine.Option(required = true, names = ["-2", "--second"], description = "Second file path")
    void setSecondFile(File secondFile)
    {
        ensureFile(secondFile);
        this.secondFile = secondFile;
    }

    boolean isPrintDiff()
    {
        return printDiff;
    }

    void setPrintDiff(boolean printDiff)
    {
        this.printDiff = printDiff;
    }

    boolean isPrintCommonLines()
    {
        return printCommonLines;
    }

    void setPrintCommonLines(boolean printCommonLines)
    {
        this.printCommonLines = printCommonLines;
    }

    boolean isTrim()
    {
        return trim;
    }

    void setTrim(boolean trim)
    {
        this.trim = trim;
    }

    @Override
    void run()
    {
        Set<String> first = firstFile.readLines().toSet()
        Set<String> second = secondFile.readLines().toSet()

        if (printDiff) {
            Set<String> firstCopy = new HashSet<>(first)
            firstCopy.removeAll(second)
            firstCopy.printAsLines("ONLY IN FIRST   : ")

            Set<String> secondCopy = new HashSet<>(second)
            secondCopy.removeAll(first)
            secondCopy.printAsLines("ONLY IN SECOND  : ")
        }

        if (printCommonLines) {
            Set<String> firstCopy = new HashSet<>(first)
            firstCopy.retainAll(second)
            firstCopy.printAsLines("COMMON LINE     : ")
        }


    }
}
