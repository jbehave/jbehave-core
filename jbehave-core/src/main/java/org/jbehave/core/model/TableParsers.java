package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.jbehave.core.model.ExamplesTable.PropertiesData;
import org.jbehave.core.model.ExamplesTable.RowsData;

public class TableParsers {

    private static final String ROW_SEPARATOR_PATTERN = "\r?\n";

    public TableParsers(){}

    public PropertiesData parseProperties(String tableAsString, Keywords keywords) {
        return parseProperties(tableAsString, keywords.examplesTableHeaderSeparator(), keywords.examplesTableValueSeparator(),
                keywords.examplesTableIgnorableSeparator());
    }

    public PropertiesData parseProperties(String tableAsString, String headerSeparator, String valueSeparator,
                                                        String ignorableSeparator) {
        Deque<ExamplesTableProperties> properties = new LinkedList<>();
        String tableWithoutProperties = tableAsString.trim();
        Matcher matcher = ExamplesTable.INLINED_PROPERTIES_PATTERN.matcher(tableWithoutProperties);
        while (matcher.matches()) {
            String propertiesAsString = matcher.group(1);
            propertiesAsString = StringUtils.replace(propertiesAsString, "\\{", "{");
            propertiesAsString = StringUtils.replace(propertiesAsString, "\\}", "}");
            properties.add(new ExamplesTableProperties(propertiesAsString, headerSeparator,
                    valueSeparator, ignorableSeparator));
            tableWithoutProperties = matcher.group(2).trim();
            matcher = ExamplesTable.INLINED_PROPERTIES_PATTERN.matcher(tableWithoutProperties);
        }
        if (properties.isEmpty()) {
            properties.add(new ExamplesTableProperties("", headerSeparator, valueSeparator, ignorableSeparator));
        }
        return new ExamplesTable.PropertiesData(tableWithoutProperties, properties);
    }

    public RowsData parseByRows(String tableAsString, ExamplesTableProperties properties) {
        List<String> headers = new ArrayList<>();
        List<Map<String, String>> data = new ArrayList<>();

        String[] rows = tableAsString.split(ROW_SEPARATOR_PATTERN);
        for (String row : rows) {
            if (row.startsWith(properties.getIgnorableSeparator()) || row.isEmpty()) {
                // skip ignorable or empty lines
                continue;
            } else if (headers.isEmpty()) {
                headers.addAll(parseRow(row, true, properties));
            } else {
                List<String> columns = parseRow(row, false, properties);
                Map<String, String> map = new LinkedHashMap<>();
                for (int column = 0; column < columns.size(); column++) {
                    if (column < headers.size()) {
                        map.put(headers.get(column), columns.get(column));
                    }
                }
                data.add(map);
            }
        }

        return new RowsData(headers, data);
    }

    public List<String> parseRow(String rowAsString, boolean header, ExamplesTableProperties properties) {
        String separator = header ? properties.getHeaderSeparator() : properties.getValueSeparator();
        return parseRow(rowAsString, separator, properties.getCommentSeparator(), properties.isTrim());
    }

    private List<String> parseRow(String rowAsString, String separator, String commentSeparator,
            boolean trimValues) {
        StringBuilder regex = new StringBuilder();
        for (char c : separator.toCharArray()) {
            regex.append("\\").append(c);
        }
        List<String> values = new ArrayList<>();
        for (String value : rowAsString.split(regex.toString(), -1)) {
            String stripped = StringUtils.substringBefore(value, commentSeparator);
            String trimmed = trimValues ? stripped.trim() : stripped;
            values.add(StringUtils.substringBefore(trimmed, commentSeparator));
        }
        // ignore a leading and a trailing empty value
        if (StringUtils.isBlank(values.get(0))) {
            values.remove(0);
        }
        int lastIndex = values.size() - 1;
        if (lastIndex != -1 && StringUtils.isBlank(values.get(lastIndex))) {
            values.remove(lastIndex);
        }
        return values;
    }
}
