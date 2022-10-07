package gilli.sci.formula

class Area
{
    static BigDecimal ofCircleWithRadius(Number radius)
    {
        Math.PI * radius * radius
    }

    static BigDecimal ofCircleWithDiameter(Number dia)
    {
        ofCircleWithRadius(dia / 2)
    }
}
