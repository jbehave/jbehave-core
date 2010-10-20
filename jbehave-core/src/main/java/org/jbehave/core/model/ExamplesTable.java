package org.jbehave.core.model;

import static java.lang.Boolean.parseBoolean;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
 * By default all column values are trimmed. To avoid trimming the values:
 * 
 * <pre>
 * {trim=false}
 * | header 1 | header 2 | .... | header n |
 * | value 11 | value 12 | .... | value 1n |
 * </pre>
 * 
 * </p>
 */
public class ExamplesTable {

    private static final String HEADER_SEPARATOR = "|";
    private static final String VALUE_SEPARATOR = "|";
    private static final String IGNORABLE_SEPARATOR = "|--";
    private final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    private final String tableAsString;
    private final String headerSeparator;
    private final String valueSeparator;
    private final String ignorableSeparator;
    private final List<String> headers = new ArrayList<String>();
    private final Properties properties = new Properties();
    private boolean trim = true;

    public ExamplesTable(String tableAsString) {
        this(tableAsString, HEADER_SEPARATOR, VALUE_SEPARATOR);
    }

    public ExamplesTable(String tableAsString, String headerSeparator, String valueSeparator) {
        this(tableAsString, headerSeparator, valueSeparator, IGNORABLE_SEPARATOR);
    }

    public ExamplesTable(String tableAsString, String headerSeparator, String valueSeparator, String ignorableSeparator) {
        this.tableAsString = tableAsString;
        this.headerSeparator = headerSeparator;
        this.valueSeparator = valueSeparator;
        this.ignorableSeparator = ignorableSeparator;
        parse();
    }

    private void parse() {
        data.clear();
        headers.clear();
        String[] rows = splitInRows(stripProperties(tableAsString.trim()));
        for (int row = 0; row < rows.length; row++) {
            String rowAsString = rows[row];
            if (rowAsString.startsWith(ignorableSeparator)) {
                // skip rows that start with ignorable separator
                continue;
            } else if (row == 0) {
                List<String> columns = columnsFor(rowAsString, headerSeparator);
                headers.addAll(columns);
            } else {
                List<String> columns = columnsFor(rowAsString, valueSeparator);
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
            properties.load(new ByteArrayInputStream(propertiesAsString.replace(",", "\n").getBytes()));
        } catch (IOException e) {
            // carry on
        }
        trim = parseBoolean(properties.getProperty("trim", "true"));
    }

    private String[] splitInRows(String table) {
        return table.split("\n");
    }

    private List<String> columnsFor(String row, String separator) {
        List<String> columns = new ArrayList<String>();
        // use split limit -1 to ensure that empty strings will not be discarted
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

    public Properties getProperties() {
        return properties;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public Map<String, String> getRow(int row) {
        return data.get(row);
    }

    public int getRowCount() {
        return data.size();
    }

    public List<Map<String, String>> getRows() {
        return data;
    }

    public String getHeaderSeparator() {
        return headerSeparator;
    }

    public String getValueSeparator() {
        return valueSeparator;
    }

    public String asString() {
        return tableAsString;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
