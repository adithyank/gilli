package lsdsl.util;

import java.math.BigDecimal;

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

}
