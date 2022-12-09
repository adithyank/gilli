package gilli.files.storage;

import gilli.internal.main.Gilli;
import gilli.util.GeneralUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(name = "findduplicates", sortOptions = false,
        //synopsisHeading = "Finds duplicate files in the given path\n",
        description     = "Finds duplicate files in the given path and offers option to delete the duplicate files by retaining only one of them in each set of Duplicate files")
public class FileDupCmd implements Runnable
{
    private List<String> directories;

    @CommandLine.Option(names = {"--delete"}, description = "Delete the duplicate files and keep only one")
    private boolean delete = false;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-d", "--dir"}, description = "Parent directory to start scanning", required = true)
    public void setDirectories(List<String> directories)
    {
        List<File> nonExistentDirs = directories.stream()
                .map(File::new)
                .filter(f -> {
                    if (f.isDirectory())
                        return true;

                    Gilli.stdout.error("Not a directory [{}]. Skipping", f);
                    return false;
                })
                .filter(GeneralUtil::fileOrDirDoesNotExist)
                .collect(Collectors.toList());

        if (!GeneralUtil.isEmpty(nonExistentDirs)) // any dirs missing
            throw new CommandLine.ParameterException(spec.commandLine(), "Path(s) '" + this.directories + "' does not exist");

        this.directories = directories;
    }

    @Override
    public void run()
    {
        try
        {
            FileDupFinder.findDup(directories, delete);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
