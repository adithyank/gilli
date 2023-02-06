package gilli.ext

import gilli.util.GeneralUtil

class GeneralStaticMethodExt
{
    static int within(Random kl, int bound)
    {
        return GeneralUtil.randomWithin(bound);
    }
}
