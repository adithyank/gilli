package gilli.util.dataframe

import gilli.util.TextUtil
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2

class DataFrameDSL
{
    static DataFrame create2dData(@DelegatesTo(DataFrame) Closure closure)
    {
        DataFrame twoDData = new DataFrame()
        return twoddata(twoDData, closure)
    }

    static DataFrame twoddata(DataFrame data, @DelegatesTo(DataFrame) Closure closure)
    {
        closure.delegate = data
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()

        return data
    }

    static DataFrame twoddata(List<String> header, List<List<Object>> data, @DelegatesTo(DataFrame) Closure closure = {})
    {
        return twoddata(new DataFrame(header, data), closure)
    }

    static DataFrame twoddata(List<List<Object>> data, @DelegatesTo(DataFrame) Closure closure = {})
    {
        return twoddata(null, data, closure)
    }
}

/**
 *
 * A Two Dimensional data structure. It has lot of utility methods to work on Two dimensional data.
 * <p>
 * Some of the examples are here
 *
 * <li> Print as table
 * <li> Print as csv
 * <li> grouping and summing
 * <li> summarizing the values using sql queries
 * <li> converting the result of any sql query to this Two dimensional structure
 * <li> column value transformation
 * <li> many more
 *
 */
@Log4j2
class DataFrame
{
    private static final String DBNAME = "twoddb"
    private static final String TABLENAMEPREFIX = "twoddb"

    private List<String> header = []

    private changeColNameCase = true

    /**
     * Every row is a Map<FieldName, FieldValue>
     */
    private List<Row> rows = []

    private Set<String> numericColumns = new HashSet<>()

    DataFrame()
    {
    }

    DataFrame(List<String> columnNames, List<List<Object>> data)
    {
        if (data == null)
            data = []

        log.trace('header = {}', columnNames)

        header(columnNames)

        data.each {
            log.trace('row = {}', it)
            row it
        }
    }

    void dontChangeColNameCase()
    {
        changeColNameCase = false
    }

    Row getFirstRow()
    {
        return rows ? rows[0] : null
    }

    Object getSingleValue()
    {
        if (rows)
            return rows[0].getFirstValue()
        else
            throw new RuntimeException("No value")
    }

    List<Row> getAllRows()
    {
        return rows
    }

    List<Object> column(String columnName)
    {
        allRows.collect {it[colname(columnName)]}
    }

    List<Object> singleColumn()
    {
        allRows.collect {it.getFirstValue()}
    }

    void row(Object... rowValues)
    {
        rows << new Row(rowValues)
    }

    void row(List<Object> rowValues)
    {
        rows << new Row(rowValues.toArray())
    }

    void row(Map<String, Object> rowValues, String placeHolderForNotAvailableValue)
    {
        def thisRow = []

        header.each { key -> thisRow << (rowValues.containsKey(key) ? rowValues[key] : placeHolderForNotAvailableValue) }

        row(thisRow)
    }

    void rows(List<Map<String, Object>> rows, String placeHolderForNotAvailableValue)
    {
        rows.each {row(it, placeHolderForNotAvailableValue)}
    }

    DataFrame newTable(Closure<Boolean> rowFilterClosure)
    {
        DataFrame n = new DataFrame()

        n.header(header)

        def filtered = rows.findAll {row -> rowFilterClosure(row)}

        filtered.each {Map row -> n.row(row, '')} //just to satisfy the compiler. empty string here does not add any value

        return n
    }

    private String colname(String colname)
    {
        return changeColNameCase ? colname.toLowerCase() : colname
    }

    void header(List<String> columnNames)
    {
        this.header.clear()
        columnNames.each {this.header << colname(it)}
    }

    void header(String... columnNames)
    {
        this.header.clear()
        header(columnNames.toList())
    }

    List<String> getHeader()
    {
        return header
    }
    Map splitByCol (String colName, boolean skipCol=true)
    {
        Map temp = [:]
        this.rows [colName].unique().each{ v ->
            temp.put v, DataFrameDSL.twoddata (this.newTable {col -> col.get(colName) == v}){
                if (skipCol)
                    removeCol(colName)
            }
        }
        temp
    }

    void removeCol (String colName)
    {
        if (! header.contains(colName))
            return
        this.header.remove(colName)
        this.rows.collect{ r -> r.remove(colName)}
    }

    void joinTable (DataFrame inputTwoD, List<String> onHeader, boolean leftJoin=false, boolean showMergedHeaders=false, Object fillStr='-')
    {
        if (!this.getHeader().containsAll(onHeader) || !inputTwoD.getHeader().containsAll(onHeader))
            return
        Map colMap = addColumns showMergedHeaders ? inputTwoD.header : (inputTwoD.header - onHeader)
        List<Row> newSet = []
        rows.each { r1 ->
            boolean isRowAdded = false
            Object[] eachR1 = r1.values().toArray()
            inputTwoD.rows.each { r2 ->
                Object[] eachR2 = r2.findAll {this.header.contains(colMap[it.key])}.values().toArray()
                if (! (onHeader.collect{ r1[it]} == onHeader.collect{ r2[it]}))
                    return
                newSet.add new Row(eachR1 + eachR2)
                isRowAdded = true
            }
            if (leftJoin && !isRowAdded)
                newSet.add new Row (eachR1 + inputTwoD.header.collect{fillStr}.toArray())
        }
        this.rows = newSet
    }
    private Map addColumns (List<String> inHeader)
    {
        Map temp = [:]
        inHeader.each{ colName ->
            temp.put colName, DataFrameColPolicy.suggestColName(colName, this.header)
            this.header.add DataFrameColPolicy.suggestColName(colName, this.header)
        }
        temp
    }

    void merge (DataFrame... inputs)
    {
        inputs.each { DataFrame input ->
            if (!(header == input.header))
                throw new IllegalArgumentException("Headers Do Not Match !!.")
        }
        inputs.each {rows.addAll(it.rows)}
    }
    void numericField(String... numericFieldNames)
    {
        numericFieldNames.each {numericColumns << it }
    }

    DataFrame transform(String columnHeaderName, Closure closure)
    {
        columnHeaderName = colname(columnHeaderName)

        rows.each {row ->

            def orig = row[columnHeaderName]
            row[columnHeaderName] = closure.call(orig)
        }

        return this
    }

    void printsv(String filename, String delimiter = ",", boolean append = false)
    {
        def stream = new PrintStream(new FileOutputStream(filename, append))
        printsv(stream, delimiter)
        stream.close()
    }

    void printsv(PrintStream printStream = System.out, String delimiter = ",")
    {
        TextUtil.printsv(this, printStream, delimiter)
    }

    void printtable(String filename, boolean append = false)
    {
        def stream = new PrintStream(new FileOutputStream(filename, append))
        printtable(stream)
        stream.close()
    }

    int getRowCount()
    {
        return rows.size()
    }

    int getColumnCount()
    {
        return header.size()
    }

    void printtable(PrintStream printStream = System.out)
    {
        TextUtil.printtable(this, printStream)
    }
    String tabletext()
    {
        TextUtil.tabletext(this)
    }

    String tabletextnoheader()
    {
        TextUtil.tabletext(this, false)
    }

    Grouped group(String groupByColumnName, Closure groupClosure = {})
    {
        groupByColumnName = colname(groupByColumnName)

        Grouped g = new Grouped(this, groupByColumnName)

        rows.each { row -> g.addRow(row[groupByColumnName], row)}

        groupClosure.delegate = g
        groupClosure.resolveStrategy = Closure.DELEGATE_ONLY
        groupClosure()

        return g
    }

    List<List<Object>> toListOfList()
    {
        List<List<Object>> ret = []

        eachRowWithIndex {list, i -> ret.add(list)}

        return ret
    }

    void eachRowWithIndex(@ClosureParams(value = SimpleType, options = ["List,Integer"]) Closure closure)
    {
        rows.eachWithIndex {o, i -> closure.call(o.values().toList(), i)}
    }

    void applysql(String sqlString)
    {
        if (rows.isEmpty())
            return

        DatabaseDSL.embeddedMemoryDerbyDB(DBNAME) {

            def fields = header

            def table = TABLENAMEPREFIX + System.currentTimeMillis()

            Map<String, String> dataTypes = [:]

            numericColumns.each {dataTypes.put(it, "NUMERIC")}

            createTableIfNotExists(table, fields, dataTypes)

            insertBatch(table, fields, toListOfList(), 100)

            sqlString = sqlString.replaceAll('__TABLE__', table)

            printtable sqlString
        }
    }

    DataFrame addDiffCol(String newColumnName, String forColumn)
    {
        addcol newColumnName, {
            prevrow ? thisrow[forColumn] - prevrow[forColumn] : 0
        }
    }

    DataFrame addPercentChangeCol(String newColumnName, String forColumn)
    {
        addcol newColumnName, {
            prevrow ? thisrow[forColumn].percentChange(prevrow[forColumn]) : 0
        }
    }

    DataFrame addCumSumCol(String newColumnName, String forColumn)
    {
        addcol newColumnName, {
            first ? thisrow[forColumn] : thisrow[forColumn] + prevrow[newColumnName]
        }
    }

    DataFrame addMACol(String newColumnName, String forColumn)
    {
        addMACol newColumnName, forColumn, 5
    }

    DataFrame addMACol(String newColumnName, String forColumn, Integer window)
    {
        if (window < 1)
            throw Exception('Invalid window size')
        List values = []
        addcol newColumnName, {
            values.add(thisrow[forColumn])
            if (rowid >= window)
                values.remove(0)
            rowid >= window-1 ? (values.sum()/window).twoDecimals() : 0
        }
    }

    /**
     * Adds a new column with the given name. The value of the column will be the returned value of the closure
     *
     *
     * @param newColumnName name of the new column that is going to be appended to this {@link DataFrame}
     * @param valueClosure The returned value of this closure will be the value of this newly added column.
     *
     * This closure will be called for each row. Below variables will be available inside the closure for using
     * <li> <b>rowid</b> : Index of the current row for which this closure is called. For first row, this value will be '0',
     * for the second row this value will be '1' and so on. The last 'rowid' value will be 'totalrows - 1'.
     * <li> <b>thisrow</b> : This is a Map with values of the current row for which the closure is called. So, you can use
     * {@code thisrow.personname} will give the value of the field 'personname' of this row. If the column header has
     * special characters you can use this syntax {@code thisrow['%age']} to use the value of the column '%age' of
     * thisrow
     * <li> <b>prevrow</b> : This is the Map with the values of the previous row. If the current row is the first row, then
     * 'prevrow' will be null
     * <li> <b>first</b> : This is a boolean value. It will be 'true' when the closure is called for the first row. If not
     * first row, this will be 'false'
     * <li> additionaly, the names of the columns are made available as direct variable names inside this closure whose
     * value will be the value of the that column in this row. For column names without special characters, using
     * like this will be very handy and make the script more readable
     * <p>
     * See the example directory for more usages
     *
     *
     * @see <a href= "http://groovy-lang.org/groovy-dev-kit.html#_map_property_notation">Groovy Tuturial For Map</a>
     * @return
     */
    DataFrame addcol(String newColumnName, Closure valueClosure)
    {
        Row prev = null

        rows.eachWithIndex { Row thisrow, int rowid ->

            Map binding = [:]
            binding.rowid = rowid
            binding.thisrow = thisrow
            binding.prevrow = prev
            binding.first = (rowid == 0)

            thisrow.each {k, v -> binding[k] =  v}

            valueClosure.delegate = binding

            Object newColValue = valueClosure()

            thisrow[newColumnName] = newColValue

            log.trace("col = {}, newval = {}", newColumnName, newColValue)

            prev = thisrow
        }

        header << newColumnName

        return this
    }

    DataFrame addPercentCol(String newColumnName, String forColumn)
    {
        rows.each {

            def val = it[forColumn]

            if (val == null)
                val = 0

            it[newColumnName] = (val as Number).percentOf(rows[forColumn].sum() as Number)
        }

        header << newColumnName

        return this
    }

    DataFrame reverse()
    {
        rows.reverse(true)
        return this
    }

    DataFrame sortby(String sortColumnName, boolean reverse = false)
    {
        rows.sort(new Comparator<Row>() {

            @Override
            int compare(Row o1, Row o2) {

                o1[sortColumnName] <=> o2[sortColumnName]
            }
        })

        if (reverse)
            rows.reverse(true)

        return this
    }

    DataFrame sortby(String sortColumnName, List<String> values)
    {
        sortColumnName = colname(sortColumnName)

        Map<String, Integer> forsort = new LinkedHashMap<>()

        values.eachWithIndex { String entry, int i -> forsort.put(entry, i)}

        rows.sort(new Comparator<Row>()
        {
            @Override
            int compare(Row o1, Row o2)
            {
                return forsort.get(o1[sortColumnName]) <=> forsort.get(o2[sortColumnName])
            }
        })

        return this
    }

    class Row extends LinkedHashMap<String, Object>
    {
        Row(Object... rowValues)
        {
            if (DataFrame.this.header.size() > rowValues.size())
                throw new Exception("Insufficient Row length (" + rowValues.size() + "). Header length : " + DataFrame.this.header.size() + ". rowValues = " + rowValues)

            DataFrame.this.header.eachWithIndex { String h, int i -> put(h, rowValues[i]) }
        }

        List<String> toStringList()
        {
            return values().collect{String.valueOf(it)}
        }

        List<Object> toList()
        {
            return values().toList()
        }

        Object getFirstValue()
        {
            return toList()[0]
        }
    }

    @Log4j2
    class Grouped
    {
        private String groupbyColumn
        private DataFrame sourceData
        private Map<Object, List<Row>> groupedRowList = [:]

        private Grouped(DataFrame sourceData, String groupbyColumn)
        {
            this.sourceData = sourceData
            this.groupbyColumn = colname(groupbyColumn)
        }

        void addRow(Object key, Map<String, Object> row)
        {
            def rows = groupedRowList[key]

            if (rows == null)
            {
                rows = []
                groupedRowList[key] = rows
            }

            rows.add(row)
        }

        List<Row> rowsFor(String groupByColumnValue)
        {
            return groupedRowList[groupByColumnValue]
        }

        DataFrame sum(String columnName)
        {
            return numericOp(columnName, 'sum') {List values -> values.sum()}
        }

        DataFrame avg(String columnName)
        {
            return numericOp(columnName, 'avg') {List values -> (double) values.sum() / (double) values.size()}
        }

        DataFrame max(String columnName)
        {
            return numericOp(columnName, 'max') {List values -> values.max()}
        }

        DataFrame min(String columnName)
        {
            return numericOp(columnName, 'min') {List values -> values.min()}
        }

        DataFrame concat(String columnName, String delimiter = ', ')
        {
            return numericOp(columnName, 'concat') {List values -> values.join(delimiter)}
        }

        DataFrame count()
        {
            return numericOp(sourceData.header[0], 'count') { List values -> values.size()}
        }

        DataFrame numericOp(String columnName, String op, Closure c)
        {
            columnName = colname(columnName)

            DataFrame twoDData = new DataFrame()

            /**
             * if two columns are same, then twoDData will getconfused as two of its columns (i.e., map keys)
             * will be same. Hence this appending
             */
            String h = op + "_" + columnName

            twoDData.header(groupbyColumn, h)

            groupedRowList.each {k, List<Row> rows ->

                def list = rows[columnName]

                log.trace "k = {}, grouped rows = {}", k, list

                twoDData.row k, c.call(list)
            }

            return twoDData
        }
    }

    enum GroupByOp
    {
        MAX,
        MIN,
        SUM,
        COUNT
    }

}
class DataFrameColPolicy
{
    final static String SEP = '_'
    static String suggestColName (String colName, List<String> header)
    {
        if (!header.contains(colName))
            return colName
        String last = colName.afterFromLast SEP
        last.isNumber() ? suggestColName("${colName.beforeLast(SEP)}_${last.toInteger()+1}", header) : suggestColName("${colName}${SEP}1", header)
    }
}