package gilli.internal.main;

import picocli.CommandLine.Command;

@Command(name = "help", description = "Prints help message", helpCommand = true)
public class HelpCommand implements Runnable
{
    @Override
    public void run()
    {
        System.out.println("Help message");
        GilliMain.instance.cmdLine.usage(System.out);
    }
}
