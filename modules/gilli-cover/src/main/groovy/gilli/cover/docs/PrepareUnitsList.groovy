package gilli.cover.docs

import gilli.cover.CoverMain
import gilli.sci.unit.UnitConversionSpec
import gilli.sci.unit.Units

class UnitsList
{

    static void prepare()
    {
        File file = DocsUtil.autoGeneratedFilePath('conversionslist.md')

        file.withWriter { writer ->

            Units.convert.registeredTypes.each {typeName ->

                writer.newLine()
                def unitType = Units.convert.type(typeName)

                writer.writeLine "# $typeName"
                writer.newLine()
                writer.writeLine "Reference Unit Name : `$unitType.referenceUnit`"
                writer.newLine()

                DocsUtil.writeTable(writer, unitType.specs) {

                    header {

                        col "Unit Type"
                        col "Unit Name"
                        col "Symbol"
                        col "Description"
                        col "Conversion Formula Description"
                        col "Conversion Factor To Reference Unit"
                    }

                    row(UnitConversionSpec) {

                        col typeName
                        col it.unitName
                        col it.symbol
                        col it.description
                        col it.conversionClosureDescription
                        col it.conversionFactorToRef
                    }
                }

                writer.newLine()
                writer.newLine()
            }
        }
    }
}
