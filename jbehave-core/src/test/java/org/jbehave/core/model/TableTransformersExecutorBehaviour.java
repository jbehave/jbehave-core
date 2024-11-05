package org.jbehave.core.model;

import static org.jbehave.core.model.ExamplesTable.TableProperties;
import static org.jbehave.core.model.ExamplesTable.TablePropertiesQueue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Deque;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;

class TableTransformersExecutorBehaviour {

    private static final TableTransformers TRANSFORMERS = new TableTransformers();
    private static final ParameterConverters PARAMETER_CONVERTERS = new ParameterConverters(
            new LoadFromClasspath(), new ParameterControls(), TRANSFORMERS, true);
    private static final TableParsers TABLE_PARSERS = new TableParsers(new LocalizedKeywords(), PARAMETER_CONVERTERS);

    @Test
    void shouldApplyTransformers() {
        String table = "|key|\n!value0!";
        String lineFromFirstTransformer = "\n!value1!";
        String lineFromSecondTransformer = "\n!value2!";
        String tableTransformed = table + lineFromFirstTransformer + lineFromSecondTransformer;
        String firstTransformerName = "FIRST_TRANSFORMER";
        String firstPropertiesAsString = String.format("transformer=%s, property=any_property1, valueSeparator=!",
                firstTransformerName);
        String secondTransformerName = "SECOND_TRANSFORMER";
        String secondPropertiesAsString = String.format(
                "transformer=%s, property=any_property2", secondTransformerName);
        TablePropertiesQueue tablePropertiesQueue = TABLE_PARSERS.parseProperties(
                  "{" + firstPropertiesAsString + "}"
                + "{" + secondPropertiesAsString + "}"
                + "\n"
                + table);
        Deque<TableProperties> tableProperties = tablePropertiesQueue.getProperties();


        TRANSFORMERS.useTransformer(firstTransformerName, (tableAsString, tableParsers, properties) ->
                tableAsString + lineFromFirstTransformer);
        TRANSFORMERS.useTransformer(secondTransformerName, (tableAsString, tableParsers, properties) ->
                tableAsString + lineFromSecondTransformer);
        TableTransformerMonitor tableTransformerMonitor = mock(TableTransformerMonitor.class);
        String output = TableTransformersExecutor.applyTransformers(
                TRANSFORMERS, table, TABLE_PARSERS, tableProperties, tableTransformerMonitor);

        assertEquals(tableTransformed, output);
        verify(tableTransformerMonitor).beforeTransformerApplying(eq(firstTransformerName), argThat(
                p -> p.getPropertiesAsString().equals(firstPropertiesAsString)), eq(table));
        verify(tableTransformerMonitor).afterTransformerApplying(eq(firstTransformerName), argThat(p ->
                p.getPropertiesAsString().equals(firstPropertiesAsString)), eq(table + lineFromFirstTransformer));
        verify(tableTransformerMonitor).beforeTransformerApplying(eq(secondTransformerName), argThat(p ->
                p.getPropertiesAsString().equals(secondPropertiesAsString)), eq(table + lineFromFirstTransformer));
        verify(tableTransformerMonitor).afterTransformerApplying(eq(secondTransformerName), argThat(
                p -> p.getPropertiesAsString().equals(secondPropertiesAsString)),
                eq(table + lineFromFirstTransformer + lineFromSecondTransformer));
    }
}
