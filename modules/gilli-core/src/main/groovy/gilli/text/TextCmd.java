package gilli.text;

import picocli.CommandLine;

@CommandLine.Command(name = "text", sortOptions = false, description = "Text Processing utilities",
subcommands = TextSplitCmd.class)
public class TextCmd
{
}
