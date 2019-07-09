package gilli.sci.conv

import gilli.util.GilliRTException
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

@CompileStatic
class Units
{
    static final Map<String, UnitType> types = [:]

    private static <T extends UnitType> T type0(String unitType, Class<T> klass)
    {
        T t = (T) types[unitType]
        if (t)
            return t

        t = klass.newInstance()
        t.type = unitType
        types[unitType] = t
        return t
    }

    static void checkValidType(String unitType)
    {
        if (!types.containsKey(unitType))
            throw new GilliRTException("Unknown UnitType : " + unitType)
    }

    static UnitValue convert_length(Number number)
    {
        new UnitValue(length, number)
    }

    static UnitType getLength()
    {
        return type0('length', LengthUnitType)
    }
}

@CompileStatic
abstract class UnitType
{
    @PackageScope
    String type
    private Map<String, UnitConversionSpec> units = new HashMap<>()

    protected abstract void registerSpecs()
    protected abstract String reference()

    UnitType()
    {
        registerSpecs()
    }

    @Override
    String toString()
    {
        type
    }

    void checkValidUnitName(String unitName)
    {
        if (!units.containsKey(standardize(unitName)))
            throw new GilliRTException("Unknown UnitName [" + unitName + "] in unitType [" + type + "]")
    }

    protected static UnitConversionSpec specOf(String unitName)
    {
        return new UnitConversionSpec(unitName)
    }

    protected register(UnitConversionSpec spec)
    {
        units[standardize(spec.unitName)] = spec

        if (spec.unitPluralName)
            units[standardize(spec.unitPluralName)] = spec
    }

    static String standardize(String unitName)
    {
        unitName.toLowerCase()
    }

    UnitConversionSpec spec(String unitName)
    {
        units[standardize(unitName)]
    }

    UnitValue to(UnitValue from, String toUnitName)
    {
        toUnitName = standardize(toUnitName)
        checkValidUnitName(toUnitName)

        Number val = from.value * (from.spec.conversionFactorToRef / spec(toUnitName).conversionFactorToRef)
        new UnitValue(from.unitType, toUnitName, val)
    }

    List<UnitConversionSpec> getSpecs()
    {
        units.values().toList()
    }

    void printSpecs()
    {
        specs.each {println it}
    }
}

@CompileStatic
class UnitValue
{
    UnitType unitType     //like Length, Time, Pressure
    String unitName       //like nanometer, seconds
    BigDecimal value

    UnitValue(UnitType unitType, Number value)
    {
        this(unitType, unitType.reference(), value)
    }

    UnitValue(UnitType unitType, String unitName, Number value)
    {
        this.unitType = unitType
        this.unitName = unitName
        this.value = value as BigDecimal
    }

    UnitConversionSpec getSpec()
    {
        unitType.spec(this.unitName)
    }

    String toString()
    {
        valueWithUnit
    }

    String getStringValue()
    {
        value.toPlainString()
    }

    String getValueWithUnit()
    {
        value.toPlainString() + ' ' + unitName
    }

    def propertyMissing(String name)
    {
        if (!name.startsWith('to'))
        {
            this.unitName = name
            return this
        }

        name = name.substring(2, name.length())
        unitType.to(this, name)
    }
}

class UnitConversionSpec
{
    String unitName
    String unitPluralName
    String description
    String symbol

    BigDecimal conversionFactorToRef
    Closure<Number> conversionClosure //one Number value will be given to the closure

    @Override
    String toString()
    {
        String s = "name: [$unitName"

        if (unitPluralName)
            s += ", $unitPluralName"

        s += ']'

        if (description)
            s += ", descr: [$description]"

        if (symbol)
            s += ", symbol: [$symbol]"

        if (conversionFactorToRef)
            s += ", conversionFactorToRef: $conversionFactorToRef"

        if (conversionClosure)
            s += ", conversion Expression Given"

        return s
    }

    UnitConversionSpec(String unitName)
    {
        this.unitName = unitName
    }

    static UnitConversionSpec of(String name)
    {
        new UnitConversionSpec(name)
    }

    UnitConversionSpec descr(String description)
    {
        this.description = description
        return this
    }

    UnitConversionSpec plural(String unitPluralName)
    {
        this.unitPluralName = unitPluralName
        return this
    }

    /**
     * The number indicates the factor that is used to convert this unitName to the reference unitName
     * <p>
     *     Ex: if this unitName is 'kilometer' and the reference unitName is 'meter', then we should register
     *     spec as
     * <br>
     *     <code>register specOf('kilometer').factorToRef(1000)</code>
     * </p>
     * @param conversionFactorToRef
     * @return
     */
    UnitConversionSpec factorToRef(BigDecimal conversionFactorToRef)
    {
        this.conversionFactorToRef = conversionFactorToRef
        return this
    }

    UnitConversionSpec factorToRef(double conversionFactorToRef)
    {
        this.conversionFactorToRef = conversionFactorToRef.toBigDecimal()
        return this
    }

    UnitConversionSpec convertUsing(@ClosureParams(value = SimpleType, options = ['java.lang.Number']) Closure<Number> closure)
    {
        this.conversionClosure = closure
        return this
    }

    UnitConversionSpec symbol(String symbol)
    {
        this.symbol = symbol
        return this
    }

    boolean getHasClosure()
    {
        conversionClosure != null
    }
}
