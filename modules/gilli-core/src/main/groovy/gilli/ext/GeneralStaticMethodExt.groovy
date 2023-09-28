package gilli.ext

import gilli.util.GeneralUtil

class GeneralStaticMethodExt
{
    static int within(Random kl, int bound)
    {
        return GeneralUtil.randomWithin(bound);
    }

    static double log(Math m, Number base, Number value)
    {
        return Math.log(value.doubleValue()) / Math.log(base.doubleValue());
    }
}
