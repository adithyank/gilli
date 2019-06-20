package lsdsl.internal.main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class LsDSL
{
    public static final String PRODUCT = "LsDSL";
    public static final String SYSPROP_PREFIX = "lsdsl";
    public static final String CMD = PRODUCT;

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
        return LsDSL.class.getClassLoader().getResourceAsStream(path);
    }

    public static Stream<String> getResourceAsLines(String path)
    {
        InputStream stream = getResourceAsStream(path);
        return new BufferedReader(new InputStreamReader(stream)).lines();
    }

}
