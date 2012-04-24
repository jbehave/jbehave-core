package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TableUtils {

    public static List<String> parseRow(String rowAsString, String separator, boolean trimValues) {
        StringBuffer regex = new StringBuffer();
        for (char c : separator.toCharArray()) {
            regex.append("\\").append(c);
        }
        List<String> values = new ArrayList<String>();
        for ( String value : rowAsString.split(regex.toString(),-1) ){
            values.add(trimValues ? value.trim() : value);
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
