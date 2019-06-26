package gilli.util;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class GeneralUtil
{
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
