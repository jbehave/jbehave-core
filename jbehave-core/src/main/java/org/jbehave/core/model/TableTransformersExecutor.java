package org.jbehave.core.model;

import static org.jbehave.core.model.ExamplesTable.TableProperties;

import java.util.Deque;

public final class TableTransformersExecutor {

    private TableTransformersExecutor() {
    }

    public static String applyTransformers(TableTransformers tableTransformers, String tableAsString,
                                           TableParsers tableParsers, Deque<TableProperties> tablePropertiesQueue,
                                           TableTransformerMonitor tableTransformerMonitor) {
        String transformedTable = tableAsString;
        TableProperties previousProperties = null;
        for (TableProperties properties : tablePropertiesQueue) {
            String transformer = properties.getTransformer();
            if (transformer != null) {
                if (previousProperties != null) {
                    properties.overrideSeparatorsFrom(previousProperties);
                }
                tableTransformerMonitor.beforeTransformerApplying(transformer, properties, transformedTable);
                transformedTable = tableTransformers.transform(transformer, transformedTable, tableParsers, properties);
                tableTransformerMonitor.afterTransformerApplying(transformer, properties, transformedTable);
            }
            previousProperties = properties;
        }
        return transformedTable;
    }
}
