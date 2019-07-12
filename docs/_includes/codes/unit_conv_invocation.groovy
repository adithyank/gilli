import gilli.sci.unit.UnitValue
import gilli.sci.unit.Units

assert convert.length(1000).meter.toMile.valueWithUnit == '0.6213712000 mile'
assert Units.convert.length(1000).meter.toMile.valueWithUnit == '0.6213712000 mile'
assert Units.convert.length(1000, 'meter').to('mile').valueWithUnit == '0.6213712000 mile'
assert Units.convert.length(1000, 'meter').toMile.valueWithUnit == '0.6213712000 mile'

//unit names are case insensitive
assert convert.length(1000).meter.toMiLE.valueWithUnit == '0.6213712000 mile'

assert convert.area(1).acre.toAre.valueWithUnit        == '40.468564224 are'

assert convert.length(1000).meter.toMiLE.value         == 0.6213712000

assert convert.area(1).acre.toAre instanceof UnitValue

