package gilli.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GeneralUtil
{
    public static boolean isScalar(Object o)
    {
        return o.getClass().isPrimitive() || o instanceof String;
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

}
