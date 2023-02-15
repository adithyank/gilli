package gilli.pwd;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "pass", sortOptions = false, description = "P a s s w o r d   M a n a g e r")
public class PasswordStore
{
    @Command(name = "list", sortOptions = false, description = "Lists all password names")
    void list()
    {
        System.out.println("list command work done");
    }
}
