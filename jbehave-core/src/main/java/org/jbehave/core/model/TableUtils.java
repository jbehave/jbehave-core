package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.ExamplesTableData;
import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;

public class TableUtils
{
    static ExamplesTableData parseData(String tableAsString, Keywords keywords) {
        return parseData(tableAsString, keywords.examplesTableHeaderSeparator(), keywords.examplesTableValueSeparator(),
                keywords.examplesTableIgnorableSeparator());
    }

    static ExamplesTableData parseData(String tableAsString, String headerSeparator, String valueSeparator,
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
        return new ExamplesTableData(tableWithoutProperties, properties);
    }

    public static List<String> parseRow(String rowAsString, boolean header, ExamplesTableProperties properties) {
        String separator = header ? properties.getHeaderSeparator() : properties.getValueSeparator();
        return parseRow(rowAsString, separator, properties.getCommentSeparator(), properties.isTrim());
    }

    private static List<String> parseRow(String rowAsString, String separator, String commentSeparator,
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
