package gilli.internal.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class Gilli
{
    public static final String PRODUCT = "Gilli";
    public static final String SYSPROP_PREFIX = "gilli";
    public static final String CMD = "gilli";

    public static Logger stdout = LogManager.getLogger("gilli.stdout");
    public static Logger stdoutWithoutTime = LogManager.getLogger("gilli.stdout.withouttime");

    public static void exitAbnormally()
    {
        exit(1);
    }

    public static void exitNormally()
    {
        exit(0);
    }

    public static void exit(int status)
    {
        System.exit(status);
    }

    public static InputStream getResourceAsStream(String path)
    {
        return Gilli.class.getClassLoader().getResourceAsStream(path);
    }

    public static Stream<String> getResourceAsLines(String path)
    {
        InputStream stream = getResourceAsStream(path);
        return new BufferedReader(new InputStreamReader(stream)).lines();
    }

}
