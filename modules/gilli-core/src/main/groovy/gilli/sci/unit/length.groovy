package gilli.sci.unit

import groovy.transform.CompileStatic

@CompileStatic
class LengthUnitType extends UnitType
{
    @Override
    protected void registerSpecs()
    {
        register specOf(reference()).factorToRef(1.0)
        register specOf('yoctometer').factorToRef(1e-24)
        register specOf('zeptometer').factorToRef(1e-21)
        register specOf('attometer').factorToRef(1e-18)
        register specOf('femtometer').factorToRef(1e-15)
        register specOf('picometer').factorToRef(1e-12)
        register specOf('nanometer').factorToRef(1e-9)
        register specOf('micrometer').factorToRef(1e-6)
        register specOf('millimeter').factorToRef(1e-3)
        register specOf('centimeter').factorToRef(1/100)
        register specOf('kilometer').factorToRef(1e3)
        register specOf('megameter').factorToRef(1e6)
        register specOf('gigameter').factorToRef(1e9)
        register specOf('terameter').factorToRef(1e12)
        register specOf('finger').factorToRef(0.022225)
        register specOf('finger_cloth').factorToRef(0.1143)
        register specOf('foot').factorToRef(0.3)
        register specOf('foot_cape').factorToRef(0.314858)
        register specOf('foot_clarke').factorToRef(0.3047972654)
        register specOf('foot_indian').factorToRef(0.304799514)
        register specOf('foot_metric').factorToRef(Math.sqrt(0.1d))
        register specOf('foot_us').factorToRef(1200/3937)
        register specOf('chain').factorToRef(spec('foot_us').conversionFactorToRef * 66)
        register specOf('furlong').factorToRef(201.168).symbol("fur")
        register specOf('hand').factorToRef(0.1016)
        register specOf('inch').factorToRef(0.0254)
        register specOf('mil').factorToRef(0.0254 / 1000)
        register specOf('league').factorToRef(4828)
        register specOf('lightsecond').factorToRef(299792458)
        register specOf('lightminute').factorToRef(299792458l * 60)
        register specOf('lighthour').factorToRef(299792458l * 60 * 60)
        register specOf('lightday').factorToRef(299792458l * 60 * 60 * 24)
        register specOf('lightyear').factorToRef(299792458l * 60 * 60 * 24 * 365.25)
        register specOf('mil').factorToRef(2.5422223123123e-5)
        register specOf('mil_sweden_and_norway').factorToRef(10000)
        register specOf('mile_geographical').factorToRef(1853.7936)
        register specOf('mile_international').factorToRef(1609.344)
        register specOf('mile').factorToRef(1609.344)
        register specOf('mile_tactical').factorToRef(1828.8)
        register specOf('mile_telegraph').factorToRef(1855.3176)
        register specOf('mile_us_survey').factorToRef(1609.347219)
        register specOf('nautical_league').factorToRef(5556).symbol("nl")
        register specOf('nautical_mile_admiralty').factorToRef(1853.184).symbol("nmi (adm)")
        register specOf('nautical_mile_us_pre1954').factorToRef(1853.248)
        register specOf('pace').factorToRef(0.762)
        register specOf('palm').factorToRef(0.0762)
        register specOf('twip').factorToRef(0.0254/1440)
        register specOf('barleycorn').factorToRef(spec('inch').conversionFactorToRef / 3).descr('One third of inch. Basis of shoe sizes in English-speaking countries')
        register specOf('astronomical_unit').factorToRef(149597870700).descr('Distance from Earth to Sun')
        register specOf('bohr').factorToRef(5.2917721092e-11).descr('Atomic Unit of Length. Bohr Radius of Hydrogen. Convenient for Atomic Physics calculations')
        register specOf('yard').factorToRef(0.9144)
    }

    @Override
    protected String reference()
    {
        return 'meter'
    }
}
