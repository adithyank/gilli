package gilli.files;

import gilli.files.storage.FileDupCmd;
import picocli.CommandLine;

@CommandLine.Command(name = "file", sortOptions = false, synopsisHeading = "Directory/File Based Operations\n\n", description = "Directory or File based utilities",
subcommands = {FileDupCmd.class})
public class FileCmd
{
}
