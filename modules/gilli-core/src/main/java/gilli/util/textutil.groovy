package gilli.util

import gilli.util.dataframe.DataFrame
import groovy.util.logging.Log4j2

@Log4j2
class TextUtil
{
    static void printsv(DataFrame data, PrintStream printStream = System.out, String delimiter = ",")
    {
        printStream.println(data.header.join(delimiter))

        data.allRows.each {printStream.println(it.toStringList().join(delimiter))}
    }

    static void printtable(DataFrame data, PrintStream printStream = System.out)
    {
        printtable(data.header, data.toListOfList(), printStream)
    }

    static void printtable(List<String> header, List<List<Object>> table, PrintStream printStream = System.out)
    {
        List lengths = getFormatLengths(header, table)
        def line = getTableBorderLine(lengths)

        printStream.println line

        printStream.println format(header, lengths)
        printStream.println line

        table.each {
            printStream.println format(it, lengths)
        }

        printStream.println line
    }
    /**
     * Recommended only for small tables. So use with more conscious.
     * @param header
     * @param table
     * @return
     */
    static String tabletext(DataFrame data, boolean isHeaderNeeded = true)
    {
        tabletext(data.header, data.toListOfList(), isHeaderNeeded)
    }

    static String tabletext(List<String> header, List<List<Object>> table, boolean isHeaderNeed = true)
    {
        List<String> tableText = []
        List lengths = getFormatLengths(header, table)
        def line = getTableBorderLine(lengths)
        if(isHeaderNeed) {
            tableText.add line
            tableText.add format(header, lengths)
        }
        tableText.add line
        table.each {
            tableText.add format(it, lengths)
        }
        tableText.add line

        tableText.join('\n')
    }

    static List getFormatLengths(List<String> header, List<List<Object>> table)
    {
        List<Integer> lengths = []
        header.eachWithIndex { String entry, int i -> lengths << String.valueOf(entry).length()}
        log.trace("lengths array = {}", lengths)
        table.each { row ->
            row.eachWithIndex { Object val, int c -> lengths[c] = Math.max(lengths[c], String.valueOf(val).length()) }
        }
        lengths
    }
    static String getTableBorderLine (List lenghts)
    {
        def lineList = []
        lenghts.each { lineList << '-' * it }
        format ( lineList, lenghts, '+', '+')
    }

    static String format(List<Object> values, List<Integer> lengths, String endString = '|', String delimiter = '|')
    {
        return format(values.toArray(), lengths, endString, delimiter)
    }

    static String format(Object[] values, List<Integer> lengths, String endString = '|', String delimiter = '|')
    {
        def ret = []

        values.eachWithIndex { Object entry, int i -> ret << String.valueOf(entry).padRight(lengths[i])}

        return endString +  ret.join(delimiter) + endString
    }
}
