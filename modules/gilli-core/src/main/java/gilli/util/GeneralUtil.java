package gilli.util;

import groovy.json.JsonOutput;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GeneralUtil
{
    public static final Random RANDOM = new Random();
    public static boolean isScalar(Object o)
    {
        return o == null || o.getClass().isPrimitive() || o instanceof String || o.getClass().isEnum() || o instanceof Number || o instanceof Boolean;
    }

    public static boolean isEmpty(Object o)
    {
        if (o == null)
            return true;

        if (o instanceof String && ((String) o).isEmpty())
            return true;

        if (o instanceof List && ((List) o).isEmpty())
            return true;

        if (o instanceof Map && ((Map) o).isEmpty())
            return true;

        return false;
    }

    public static boolean hasValue(String s)
    {
        return s != null && !s.isEmpty();
    }

    public static BigDecimal convertToBigDecimal(Object o)
    {
        try
        {
            return new BigDecimal(String.valueOf(o));
        }
        catch (NumberFormatException nfex)
        {
            return new BigDecimal(0);
        }
    }

    public static void sleep(int time, TimeUnit timeUnit)
    {
        try
        {
            Thread.sleep(timeUnit.toMillis(time));
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    public static String prettyJson(Object o)
    {
        return JsonOutput.prettyPrint(JsonOutput.toJson(o));
    }

    public static boolean fileOrDirDoesNotExist(File file)
    {
        return !file.exists();
    }

    public static <T> List<T> copyAndAppend(List<T> list, T o)
    {
        List<T> ret = new ArrayList<>(list);
        ret.add(o);
        return ret;
    }

    public static int randomWithin(int i)
    {
        return RANDOM.nextInt(i);
    }
}
