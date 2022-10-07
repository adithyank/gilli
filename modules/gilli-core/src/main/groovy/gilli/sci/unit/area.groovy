package gilli.sci.unit

import gilli.sci.formula.Area
import groovy.transform.CompileStatic

@CompileStatic
class AreaUnitType extends UnitType
{
    @Override
    protected void registerSpecs()
    {
        register specOf(reference()).factorToRef(1.0).symbol('square meter')
        register specOf('acre_international').factorToRef(4046.8564224).descr('10 square chain').symbol('ac')
        register specOf('acre').factorToRef(4046.8564224).descr('10 square chain').symbol('ac')
        register specOf('acre_us').factorToRef((Units.convert.length.spec('chain').conversionFactorToRef ** 2) * 10).descr('10 square chain of US Survey').symbol('ac')
        register specOf('are').factorToRef(100).symbol('a')
        register specOf('barn').factorToRef(10E-28).symbol('b')
        register specOf('barony').factorToRef(1.61874256896E7).descr('4000 acres')
        register specOf('board').factorToRef(7.74192E-3).descr('1 inch x 1 foot').symbol('bd')

        def inchAsMeter = Units.convert.length(1, 'inch').to('meter').value

        register specOf('circular_inch').factorToRef(Area.ofCircleWithDiameter(inchAsMeter)).descr('Circle whose diameter is an inch').symbol('circ in')
        register specOf('circular_mil').factorToRef(Area.ofCircleWithDiameter(inchAsMeter / 1000)).descr('Circle whose diameter is a mil').symbol('circ mil')
    }

    @Override
    protected String reference()
    {
        return 'square_meter'
    }
}
