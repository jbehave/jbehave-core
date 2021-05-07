package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TablePropertiesQueue;
import org.jbehave.core.model.ExamplesTable.TableRows;

public class TableParsers {

    private static final String ROW_SEPARATOR_PATTERN = "\r?\n";

    public TableParsers() {
    }

    public TablePropertiesQueue parseProperties(String tableAsString, Keywords keywords) {
        return parseProperties(tableAsString, keywords.examplesTableHeaderSeparator(), keywords.examplesTableValueSeparator(),
                keywords.examplesTableIgnorableSeparator());
    }

    public TablePropertiesQueue parseProperties(String tableAsString, String headerSeparator, String valueSeparator,
                                                              String ignorableSeparator) {
        Deque<TableProperties> properties = new LinkedList<>();
        String tableWithoutProperties = tableAsString.trim();
        Matcher matcher = ExamplesTable.INLINED_PROPERTIES_PATTERN.matcher(tableWithoutProperties);
        while (matcher.matches()) {
            String propertiesAsString = matcher.group(1);
            propertiesAsString = StringUtils.replace(propertiesAsString, "\\{", "{");
            propertiesAsString = StringUtils.replace(propertiesAsString, "\\}", "}");
            properties.add(new TableProperties(propertiesAsString, headerSeparator,
                    valueSeparator, ignorableSeparator));
            tableWithoutProperties = matcher.group(2).trim();
            matcher = ExamplesTable.INLINED_PROPERTIES_PATTERN.matcher(tableWithoutProperties);
        }
        if (properties.isEmpty()) {
            properties.add(new TableProperties("", headerSeparator, valueSeparator, ignorableSeparator));
        }
        return new TablePropertiesQueue(tableWithoutProperties, properties);
    }

    public TableRows parseRows(String tableAsString, TableProperties properties) {
        List<String> headers = new ArrayList<>();
        List<Map<String, String>> data = new ArrayList<>();

        String[] rows = tableAsString.split(ROW_SEPARATOR_PATTERN);
        for (String row : rows) {
            String trimmedRow = row.trim();
            if (trimmedRow.startsWith(properties.getIgnorableSeparator()) || trimmedRow.isEmpty()) {
                // skip ignorable or empty lines
                continue;
            } else if (headers.isEmpty()) {
                headers.addAll(parseRow(trimmedRow, true, properties));
            } else {
                List<String> columns = parseRow(trimmedRow, false, properties);
                Map<String, String> map = new LinkedHashMap<>();
                for (int column = 0; column < columns.size(); column++) {
                    if (column < headers.size()) {
                        map.put(headers.get(column), columns.get(column));
                    }
                }
                data.add(map);
            }
        }

        return new TableRows(headers, data);
    }

    public List<String> parseRow(String rowAsString, boolean header, TableProperties properties) {
        String separator = header ? properties.getHeaderSeparator() : properties.getValueSeparator();
        Function<String, String> trimmer = properties.isTrim() ? String::trim : Function.identity();
        return parseRow(rowAsString.trim(), separator, properties.getCommentSeparator(), trimmer);
    }

    private List<String> parseRow(String rowAsString, String separator, String commentSeparator,
            Function<String, String> trimmer) {
        String[] cells = StringUtils.splitByWholeSeparatorPreserveAllTokens(rowAsString, separator);
        List<String> row = new ArrayList<>(cells.length);
        for (int i = 0; i < cells.length; i++) {
            String cell = cells[i];
            cell = StringUtils.substringBefore(cell, commentSeparator);
            if ((i == 0 || i == cells.length - 1) && cell.isEmpty()) {
                continue;
            }
            row.add(trimmer.apply(cell));
        }
        return row;
    }
}
