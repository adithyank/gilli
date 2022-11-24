package gilli.pwd;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "pass", sortOptions = false, synopsisHeading = "P a s s w o r d   M a n a g e r\n\n")
public class PasswordStore
{
    @Command(name = "list", sortOptions = false, synopsisHeading = "Lists all password names\n\n")
    void list()
    {
        System.out.println("list command work done");
    }
}
