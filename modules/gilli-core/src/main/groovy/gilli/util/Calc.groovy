package gilli.util

class Calc
{
    static double yearsDoublingPrincipal(Number rateOfInterest)
    {
        return Math.log(1 + rateOfInterest/100, 2)
    }
}
