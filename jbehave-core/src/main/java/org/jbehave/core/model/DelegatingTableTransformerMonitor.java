package org.jbehave.core.model;

import java.util.Collection;
import java.util.function.Consumer;

import org.jbehave.core.model.ExamplesTable.TableProperties;

/**
 * Monitor which collects other {@link TableTransformerMonitor}-s and delegates all
 * invocations to the collected monitors.
 */
public class DelegatingTableTransformerMonitor implements TableTransformerMonitor {

    private final Collection<TableTransformerMonitor> delegates;

    /**
     * Creates DelegatingTableTransformerMonitor with a given collections of delegates
     *
     * @param delegates the {@link TableTransformerMonitor}-s to delegate to
     */
    public DelegatingTableTransformerMonitor(Collection<TableTransformerMonitor> delegates) {
        this.delegates = delegates;
    }

    @Override
    public void beforeTransformerApplying(String transformerName, TableProperties properties, String inputTable) {
        delegate(monitor -> monitor.beforeTransformerApplying(transformerName, properties, inputTable));
    }

    @Override
    public void afterTransformerApplying(String transformerName, TableProperties properties, String outputTable) {
        delegate(monitor -> monitor.afterTransformerApplying(transformerName, properties, outputTable));
    }

    private void delegate(Consumer<TableTransformerMonitor> monitorConsumer) {
        delegates.forEach(monitorConsumer);
    }
}
