package org.jbehave.core.model;

import org.jbehave.core.model.ExamplesTable.TableProperties;

/**
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null Object Pattern</a> implementation of
 * {@link TableTransformerMonitor}. Can be extended to override only the methods of interest.
 */
public class NullTableTransformerMonitor implements TableTransformerMonitor {

    @Override
    public void beforeTransformerApplying(String transformerName, TableProperties properties, String inputTable) {
        // Do nothing by default
    }

    @Override
    public void afterTransformerApplying(String transformerName, TableProperties properties, String outputTable) {
        // Do nothing by default
    }
}
