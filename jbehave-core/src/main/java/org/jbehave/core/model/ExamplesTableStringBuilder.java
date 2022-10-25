package org.jbehave.core.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;

public final class ExamplesTableStringBuilder {

    private ExamplesTableStringBuilder() {
    }

    public static String buildExamplesTableString(TableProperties properties, List<String> headers,
            List<List<String>> rows) {
        String headerSeparator = properties.getHeaderSeparator();
        String valueSeparator = properties.getValueSeparator();
        String rowSeparator = properties.getRowSeparator();

        StringBuilder tableBuilder = new StringBuilder();
        headers.forEach(header -> tableBuilder.append(headerSeparator).append(header));
        tableBuilder.append(headerSeparator).append(rowSeparator);

        for (List<String> row : rows) {
            for (int i = 0, headersSize = headers.size(); i < headersSize; i++) {
                tableBuilder.append(valueSeparator).append(i < row.size() ? row.get(i) : StringUtils.EMPTY);
            }
            tableBuilder.append(valueSeparator).append(rowSeparator);
        }

        return tableBuilder.toString();
    }
}
