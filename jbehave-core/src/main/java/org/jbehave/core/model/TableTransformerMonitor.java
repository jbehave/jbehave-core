package org.jbehave.core.model;

import org.jbehave.core.model.ExamplesTable.TableProperties;

/**
 * Interface to monitor table transformer events
 */
public interface TableTransformerMonitor {

    void beforeTransformerApplying(String transformerName, TableProperties properties, String inputTable);

    void afterTransformerApplying(String transformerName, TableProperties properties, String outputTable);
}
