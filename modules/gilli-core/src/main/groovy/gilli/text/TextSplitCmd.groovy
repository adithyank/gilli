package gilli.text

import gilli.util.Stdin;
import picocli.CommandLine;

@CommandLine.Command(name = "split", sortOptions = false, description = "Split text")
class TextSplitCmd implements Runnable
{
    @CommandLine.Option(required = true, names = ["-d", "--delimiter"], description = "Delimiter for Splitting. Default is Space character", defaultValue = " ")
    private String delimiter;

    @CommandLine.Option(required = true, names = ["-f", "--fields"], description = "Output fields")
    private String fields;

    @Override
    void run()
    {
        Stdin.lines().each {
            println it.splitc(delimiter)
        }
    }
}
