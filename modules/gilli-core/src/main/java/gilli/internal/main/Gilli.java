package gilli.internal.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Stream;

public class Gilli
{
    public static final String PRODUCT = "Gilli";
    public static final String SYSPROP_PREFIX = "gilli";
    public static final String CMD = "gilli";

    public static final String SCRIPT_EXTENSION = CMD;

    public static Logger stdout = LogManager.getLogger("gilli.stdout");
    public static Logger stdoutWithoutTime = LogManager.getLogger("gilli.stdout.withouttime");

    private static final String GILLI_HOME = System.getProperty("gilli.home");
    private static final String CMD_INVOCATION_DIR = System.getProperty("gilli.invocation.dir");

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

    public static boolean validScriptExtension(File file)
    {
        return file.getName().endsWith("gilli");
    }

    public static boolean invalidScriptExtension(File file)
    {
        return !validScriptExtension(file);
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

    static File home()
    {
        return new File(GILLI_HOME);
    }

    static File fileFromHome(String path)
    {
        return new File(GILLI_HOME + File.separator + path);
    }

    static String tmpDir()
    {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * At Sandbox mode, gilli script is executed from another directory. That time, working directory will be that
     * another directory and the invocation directory will be the directory from which the gilli command was given.
     *
     * When not in sandbox mode, invocation directory and working directory will be same
     * @return
     */
    static String workingDir()
    {
        String path = System.getProperty("user.dir");
        return trimPath(path);
    }

    static String trimPath(String path)
    {
        try
        {
            return new File(path).getCanonicalPath();
        }
        catch (Exception ex) {
            return path;
        }
    }

    static String invocationDir()
    {
        return trimPath(CMD_INVOCATION_DIR);
    }

    static String pathFromInvocationDir(String path)
    {
        return relativePath(invocationDir(), path);
    }

    static String relativePath(String parent, String child)
    {
        child = child.replace('/', File.separatorChar);

        File f = new File(child);

        if (f.isAbsolute())
            return child;

        return parent + File.separator + child;
    }

    public static File fileFromInvocationDir(String path)
    {
        return new File(pathFromInvocationDir(path));
    }

    public static File fileFromInvocationDir(File file)
    {
        return new File(pathFromInvocationDir(file.getAbsolutePath()));
    }

    public static String pathFromCurDir(String path)
    {
        return relativePath(workingDir(), path);
    }

    public static URL getResource(String path)
    {
        return Gilli.class.getClassLoader().getResource(path);
    }

}
