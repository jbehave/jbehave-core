package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>
 * Represents a tabular structure to hold example data for parameters named via the headers:
 * <p/>
 * <pre>
 * |header 1|header 2| .... |header n|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |value m1|value m2| .... |value mn|
 * </pre>
 * <p>Different header and value column separators can be specified to replace the default separator "|":</p>
 * <pre>
 * !!header 1!!header 2!! .... !!header n!!
 * !value 11!value 12! .... !value 1n!
 * ...
 * !value m1!value m2| .... !value mn!
 * </pre>
 * <p>Rows starting with an ignorable separator are allowed and ignored:</p>
 * <pre>
 * |header 1|header 2| .... |header n|
 * |-- A commented row --|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |-- Another commented row --|
 * |value m1|value m2| .... |value mn|
 * </pre>
 * <p>Ignorable separator is configurable and defaults to "|--".</p>
 */
public class ExamplesTable {

    private static final String NEWLINE = "\n";
    private static final String HEADER_SEPARATOR = "|";
    private static final String VALUE_SEPARATOR = "|";
    private static final String IGNORABLE_SEPARATOR = "|--";
    private final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    private final String tableAsString;
    private final String headerSeparator;
    private final String valueSeparator;
    private final String ignorableSeparator;
    private final List<String> headers = new ArrayList<String>();

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
        String[] rows = tableAsString.trim().split(NEWLINE);
        headers.clear();
        for (int row = 0; row < rows.length; row++) {
            String rowAsString = rows[row];
            if ( rowAsString.startsWith(ignorableSeparator) ) {
                // skip rows that start with ignore separator
                continue;
            } else if (row == 0) {
                List<String> columns = columnsFor(rowAsString, headerSeparator);
                headers.addAll(columns);
            } else {
                List<String> columns = columnsFor(rowAsString, valueSeparator);
                Map<String, String> map = new HashMap<String, String>();
                for (int column = 0; column < columns.size(); column++) {
                    map.put(headers.get(column), columns.get(column));
                }
                data.add(map);
            }
        }
    }

    private List<String> columnsFor(String row, String separator) {
        List<String> columns = new ArrayList<String>();
        for (String column : row.split(buildRegex(separator))) {
            columns.add(column.trim());
        }
        // we'll always have a leading column to the left side of the separator which we ignore
        columns.remove(0);
        return columns;
    }

    private String buildRegex(String separator) {
        char[] chars = separator.toCharArray();
        StringBuffer sb = new StringBuffer();
        for ( char c : chars ){
            sb.append("\\").append(c);
        }
        return sb.toString();
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
