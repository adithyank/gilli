package gilli.cover;

import gilli.cover.docs.UnitsList;
import gilli.shell.GilliShell;

public class CoverMain
{
    public static String projectDir;

    public static void main(String[] args)
    {
        System.out.println("COVER : Starting");
        GilliShell shell = new GilliShell();

        projectDir = args[0];

        UnitsList.prepare();
    }
}
