package org.jbehave.core.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.steps.ChainedRow;
import org.jbehave.core.steps.ConvertedParameters;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Parameters;
import org.jbehave.core.steps.Row;

import static java.lang.Boolean.parseBoolean;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

/**
 * <p>
 * Represents a tabular structure that holds rows of example data for parameters
 * named via the column headers:
 * <p/>
 * 
 * <pre>
 * |header 1|header 2| .... |header n|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |value m1|value m2| .... |value mn|
 * </pre>
 * <p>
 * Different header and value column separators can be specified to replace the
 * default separator "|":
 * </p>
 * 
 * <pre>
 * !!header 1!!header 2!! .... !!header n!!
 * !value 11!value 12! .... !value 1n!
 * ...
 * !value m1!value m2| .... !value mn!
 * </pre>
 * <p>
 * Rows starting with an ignorable separator are allowed and ignored:
 * </p>
 * 
 * <pre>
 * |header 1|header 2| .... |header n|
 * |-- A commented row --|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |-- Another commented row --|
 * |value m1|value m2| .... |value mn|
 * </pre>
 * <p>
 * Ignorable separator is configurable and defaults to "|--".
 * </p>
 * <p>
 * The separators are also configurable via inlined properties: 
 * <pre>
 * {ignorableSeparator=!--,headerSeparator=!,valueSeparator=!}
 * !header 1!header 2! .... !header n!
 * !-- A commented row --!
 * !value 11!value 12! .... !value 1n!
 * ...
 * !-- Another commented row --!
 * !value m1!value m2! .... !value mn!
 * </pre>
 * 
 * </p>
 * <p>
 * By default all column values are trimmed. To avoid trimming the values:
 * 
 * <pre>
 * {trim=false}
 * | header 1 | header 2 | .... | header n |
 * | value 11 | value 12 | .... | value 1n |
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * The table also allows the retrieval of row values as converted parameters.
 * Use {@link #getRowAsParameters(int)} and invoke
 * {@link Parameters#valueAs(String, Class)} specifying the header and the class
 * type of the parameter.
 * </p>
 * 
 * <p>
 * Once created, the table row can be modified, via the
 * {@link #withRowValues(int, Map)} method, by specifying the map of row values
 * to be changed.
 * </p>
 * 
 * <p>
 * A table can also be created by providing the entire data content, via the
 * {@link #withRows(List<Map<String,String>>)} method.
 */
public class ExamplesTable {
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
    private static final String EMPTY_VALUE = "";

    public static final ExamplesTable EMPTY = new ExamplesTable("");

    private static final String ROW_SEPARATOR = "\n";
    private static final String HEADER_SEPARATOR = "|";
    private static final String VALUE_SEPARATOR = "|";
    private static final String IGNORABLE_SEPARATOR = "|--";

    private final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    private final String tableAsString;
    private final String headerSeparator;
    private final String valueSeparator;
    private final String ignorableSeparator;
    private final ParameterConverters parameterConverters;
    private final List<String> headers = new ArrayList<String>();
    private final Properties properties = new Properties();
    private Map<String, String> namedParameters = new HashMap<String, String>();
    private boolean trim = true;

    private final Row defaults;

    public ExamplesTable(String tableAsString) {
        this(tableAsString, HEADER_SEPARATOR, VALUE_SEPARATOR);
    }

    public ExamplesTable(String tableAsString, String headerSeparator, String valueSeparator) {
        this(tableAsString, headerSeparator, valueSeparator, IGNORABLE_SEPARATOR, new ParameterConverters());
    }

    public ExamplesTable(String tableAsString, String headerSeparator, String valueSeparator,
            String ignorableSeparator, ParameterConverters parameterConverters) {
        this.tableAsString = tableAsString;
        this.headerSeparator = headerSeparator;
        this.valueSeparator = valueSeparator;
        this.ignorableSeparator = ignorableSeparator;
        this.parameterConverters = parameterConverters;
        this.defaults = new ConvertedParameters(EMPTY_MAP, parameterConverters);
        parse(tableAsString);
    }

    private ExamplesTable(ExamplesTable other, Row defaults) {
        this.data.addAll(other.data);
        this.tableAsString = other.tableAsString;
        this.headerSeparator = other.headerSeparator;
        this.valueSeparator = other.valueSeparator;
        this.ignorableSeparator = other.ignorableSeparator;
        this.parameterConverters = other.parameterConverters;
        this.headers.addAll(other.headers);
        this.properties.putAll(other.properties);
        this.defaults = defaults;
    }

    private void parse(String tableAsString) {
        data.clear();
        headers.clear();
        String[] rows = splitInRows(stripProperties(tableAsString.trim()));
        for (int row = 0; row < rows.length; row++) {
            String rowAsString = rows[row];
            if (rowAsString.startsWith(properties.getProperty("ignorableSeparator", ignorableSeparator)) || rowAsString.length()==0) {
                // skip empty lines and rows that start with ignorable separator
                continue;
            } else if (row == 0) {
                List<String> columns = columnsFor(rowAsString, properties.getProperty("headerSeparator", headerSeparator));
                headers.addAll(columns);
            } else {
                List<String> columns = columnsFor(rowAsString, properties.getProperty("valueSeparator", valueSeparator));
                Map<String, String> map = createRowMap();
                for (int column = 0; column < columns.size(); column++) {
                    map.put(headers.get(column), columns.get(column));
                }
                data.add(map);
            }
        }
    }

    private String stripProperties(String tableAsString) {
        Pattern pattern = compile("\\{(.*?)\\}\\s*(.*)", DOTALL);
        Matcher matcher = pattern.matcher(tableAsString);
        if (matcher.matches()) {
            parseProperties(matcher.group(1));
            return matcher.group(2);
        }
        return tableAsString;
    }

    private void parseProperties(String propertiesAsString) {
        properties.clear();
        try {
            properties.load(new ByteArrayInputStream(propertiesAsString.replace(",", ROW_SEPARATOR).getBytes()));
        } catch (IOException e) {
            // carry on
        }
        trim = parseBoolean(properties.getProperty("trim", "true"));
    }

    private String[] splitInRows(String table) {
        return table.split(ROW_SEPARATOR);
    }

    private List<String> columnsFor(String row, String separator) {
        List<String> columns = new ArrayList<String>();
        // use split limit -1 to ensure that empty strings will not be discarded
        for (String column : row.split(buildRegex(separator), -1)) {
            columns.add(valueOf(column));
        }
        // there may be a leading and a trailing empty column which we ignore
        if (StringUtils.isBlank(columns.get(0))) {
            columns.remove(0);
        }
        int lastIndex = columns.size() - 1;
        if (lastIndex != -1 && StringUtils.isBlank(columns.get(lastIndex))) {
            columns.remove(lastIndex);
        }
        return columns;
    }

    private String valueOf(String column) {
        return trim ? column.trim() : column;
    }

    private String buildRegex(String separator) {
        char[] chars = separator.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (char c : chars) {
            sb.append("\\").append(c);
        }
        return sb.toString();
    }

    protected Map<String, String> createRowMap() {
        return new LinkedHashMap<String, String>();
    }

    public ExamplesTable withDefaults(Parameters defaults) {
        return new ExamplesTable(this, new ChainedRow(defaults, this.defaults));
    }

    public ExamplesTable withNamedParameters(Map<String, String> namedParameters) {
        this.namedParameters = namedParameters;
        return this;
    }

    public ExamplesTable withRowValues(int row, Map<String, String> values) {
        getRow(row).putAll(values);
        for (String header : values.keySet()) {
            if (!headers.contains(header)) {
                headers.add(header);
            }
        }
        return this;
    }

    public ExamplesTable withRows(List<Map<String, String>> values) {
        this.data.clear();
        this.data.addAll(values);
        this.headers.clear();
        this.headers.addAll(values.get(0).keySet());
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public Map<String, String> getRow(int row) {
        if (row > data.size() - 1) {
            throw new RowNotFound(row);
        }
        Map<String, String> values = data.get(row);
        if (headers.size() != values.keySet().size()) {
            for (String header : headers) {
                if (!values.containsKey(header)) {
                    values.put(header, EMPTY_VALUE);
                }
            }
        }
        return values;
    }

    public Parameters getRowAsParameters(int row) {
        return getRowAsParameters(row, false);
    }

    public Parameters getRowAsParameters(int row, boolean replaceNamedParameters) {
        Map<String, String> rowValues = getRow(row);
        return createParameters((replaceNamedParameters ? replaceNamedParameters(rowValues) : rowValues));
    }

    private Map<String, String> replaceNamedParameters(Map<String, String> row) {
        Map<String, String> replaced = new HashMap<String, String>();
        for (String key : row.keySet()) {
            String replacedValue = row.get(key);
            for (String namedKey : namedParameters.keySet()) {
                String namedValue = namedParameters.get(namedKey);
                replacedValue = replacedValue.replaceAll(namedKey, namedValue);
            }
            replaced.put(key, replacedValue);
        }
        return replaced;
    }

    public int getRowCount() {
        return data.size();
    }

    public List<Map<String, String>> getRows() {
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        for (int row = 0; row < getRowCount(); row++) {
            rows.add(getRow(row));
        }
        return rows;
    }

    public List<Parameters> getRowsAsParameters() {
        return getRowsAsParameters(false);
    }

    public List<Parameters> getRowsAsParameters(boolean replaceNamedParameters) {
        List<Parameters> rows = new ArrayList<Parameters>();

        for (int row = 0; row < getRowCount(); row++) {
            rows.add(getRowAsParameters(row, replaceNamedParameters));
        }

        return rows;
    }

    private Parameters createParameters(Map<String, String> values) {
        return new ConvertedParameters(new ChainedRow(new ConvertedParameters(values, parameterConverters), defaults),
                parameterConverters);
    }

    public String getHeaderSeparator() {
        return headerSeparator;
    }

    public String getValueSeparator() {
        return valueSeparator;
    }

    public String asString() {
        if (data.isEmpty()) {
            return EMPTY_VALUE;
        }
        return format();
    }

    public void outputTo(PrintStream output){
        output.print(asString());
    }
    
    private String format() {
        StringBuffer sb = new StringBuffer();
        for (String header : headers) {
            sb.append(headerSeparator).append(header);
        }
        sb.append(headerSeparator).append(ROW_SEPARATOR);
        for (Map<String, String> row : getRows()) {
            for (String header : headers) {
                sb.append(valueSeparator);
                sb.append(row.get(header));
            }
            sb.append(valueSeparator).append(ROW_SEPARATOR);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @SuppressWarnings("serial")
    public static class RowNotFound extends RuntimeException {

        public RowNotFound(int row) {
            super(Integer.toString(row));
        }

    }
}
