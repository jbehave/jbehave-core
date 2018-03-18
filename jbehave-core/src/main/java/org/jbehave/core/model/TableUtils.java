package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class TableUtils
{
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
        List<String> values = new ArrayList<String>();
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
