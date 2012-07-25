package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.lang.Boolean.parseBoolean;

/**
 * <p>
 * Facade responsible for transforming table string representations. It allows
 * the registration of several {@link TableTransformer} instances by name.
 * </p>
 * <p>
 * Some Transformers are provided out-of-the-box:
 * <ul>
 * <li>{@link TableTransformers.FromLandscape FromLandscape}: registered under
 * name {@link TableTransformers#FROM_LANDSCAPE}</li>
 * </ul>
 * </p>
 */
public class TableTransformers {

    public static final String FROM_LANDSCAPE = "FROM_LANDSCAPE";
    private final Map<String, TableTransformer> transformers = new HashMap<String, TableTransformer>();

    public TableTransformers() {
        useTransformer(FROM_LANDSCAPE, new FromLandscape());
    }

    public String transform(String transformerName, String tableAsString, Properties properties) {
        TableTransformer transformer = transformers.get(transformerName);
        if (transformer != null) {
            return transformer.transform(tableAsString, properties);
        }
        return tableAsString;
    }

    public void useTransformer(String name, TableTransformer transformer) {
        transformers.put(name, transformer);
    }

    public interface TableTransformer {
        String transform(String tableAsString, Properties properties);
    }

    public static class FromLandscape implements TableTransformer {

        private static final String ROW_SEPARATOR = "\n";

        public String transform(String tableAsString, Properties properties) {
            boolean trim = parseBoolean(properties.getProperty("trim", "true"));
            String ignorableSeparator = properties.getProperty("ignorableSeparator", "|--");
            String headerSeparator = properties.getProperty("headerSeparator", "|");
            String valueSeparator = properties.getProperty("valueSeparator", "|");
            Map<String, List<String>> data = new LinkedHashMap<String, List<String>>();
            String[] rowsAsString = tableAsString.split(ROW_SEPARATOR);
            for (int row = 0; row < rowsAsString.length; row++) {
                String rowAsString = rowsAsString[row];
                if (rowAsString.startsWith(ignorableSeparator) || rowAsString.length() == 0) {
                    // skip ignorable or empty lines
                    continue;
                } else {
                    List<String> values = TableUtils.parseRow(rowAsString, valueSeparator, trim);
                    String header = values.get(0);
                    List<String> rowValues = new ArrayList<String>(values);
                    rowValues.remove(0);
                    data.put(header, rowValues);
                }
            }
            StringBuffer sb = new StringBuffer();
            int numberOfRows = 1;
            sb.append(headerSeparator);
            for (String header : data.keySet()) {
                sb.append(header).append(headerSeparator);
                numberOfRows = data.get(header).size();
            }
            sb.append(ROW_SEPARATOR);
            for (int r = 0; r < numberOfRows; r++) {
                sb.append(valueSeparator);
                for (List<String> rows : data.values()) {
                    sb.append(rows.get(r)).append(valueSeparator);
                }
                sb.append(ROW_SEPARATOR);
            }
            return sb.toString();
        }

    }

}
