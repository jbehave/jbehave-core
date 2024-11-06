package org.jbehave.core.model;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExamplesTableFactoryBehaviour {

    private static final String TABLE_AS_STRING = "|one|two|\n|11|22|\n";

    private static final String RESOURCE_PATH = "/path/to/table";

    private static final String PROPERTIES =
            "{ignorableSeparator=!--,headerSeparator=!,valueSeparator=!,commentSeparator=#}\n";

    private static final String TABLE_WITH_INLINED_SEPARATTORS = PROPERTIES
            + "!header 1!header 2! .... !header n!\n"
            + "!-- An ignored row --!\n"
            + "!value 11#comment in value!value 12! .... !value 1n!\n"
            + "!-- Another ignored row --!\n"
            + "!value m1!value m2! .... !value mn!\n";

    private static final String FILTERED_TABLE_WITH_INLINED_SEPARATTORS = PROPERTIES
            + "!header 1!header 2!....!header n!\n"
            + "!value 11!value 12!....!value 1n!\n"
            + "!value m1!value m2!....!value mn!\n";

    @Test
    void shouldCreateExamplesTableFromTableInput() {
        // Given
        ExamplesTableFactory factory = new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers());
        
        // When        
        ExamplesTable examplesTable = factory.createExamplesTable(TABLE_AS_STRING);
        
        // Then
        assertThat(examplesTable.asString(), equalTo(TABLE_AS_STRING));
    }

    @Test
    void shouldCreateExamplesTableFromResourceInput() {
        // Given
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ExamplesTableFactory factory = new ExamplesTableFactory(resourceLoader, new TableTransformers());
        
        // When
        when(resourceLoader.loadResourceAsText(RESOURCE_PATH)).thenReturn(TABLE_AS_STRING);
        ExamplesTable examplesTable = factory.createExamplesTable(RESOURCE_PATH);
        
        // Then
        assertThat(examplesTable.asString(), equalTo(TABLE_AS_STRING));
    }

    @Test
    void shouldCreateExamplesTableWithParametersFromResourceInput() {
        // Given
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ExamplesTableFactory factory = new ExamplesTableFactory(resourceLoader, new TableTransformers());
        String tableWithChangedSeparators = PROPERTIES + "!one!two!\n!11!22!\n";

        // When
        when(resourceLoader.loadResourceAsText(RESOURCE_PATH)).thenReturn(tableWithChangedSeparators);
        ExamplesTable examplesTable = factory.createExamplesTable(RESOURCE_PATH);

        // Then
        assertThat(examplesTable.asString(), equalTo(tableWithChangedSeparators));
    }

    @Test
    void shouldCreateExamplesTableFromResourceInputWithPreceedingLineBreaks() {
        // Given
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ExamplesTableFactory factory = new ExamplesTableFactory(resourceLoader, new TableTransformers());
        
        // When
        when(resourceLoader.loadResourceAsText(RESOURCE_PATH)).thenReturn(TABLE_AS_STRING);
        ExamplesTable examplesTable = factory.createExamplesTable("\n\n\n/path/to/table");
        
        // Then
        assertThat(examplesTable.asString(), equalTo(TABLE_AS_STRING));
    }

    @Test
    void shouldCreateExamplesTableFromTableInputWithInlinedSeparators() {
        // Given
        ExamplesTableFactory factory = new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers());

        // When
        ExamplesTable examplesTable = factory.createExamplesTable(TABLE_WITH_INLINED_SEPARATTORS);

        // Then
        assertThat(examplesTable.asString(), equalTo(FILTERED_TABLE_WITH_INLINED_SEPARATTORS));
    }

    @Test
    void shouldCreateExamplesTableFromTableInputWithInlinedSeparatorsHavingSpacesAtStart() {
        // Given
        ExamplesTableFactory factory = new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers());

        // When
        ExamplesTable examplesTable = factory.createExamplesTable(" " + TABLE_WITH_INLINED_SEPARATTORS);

        // Then
        assertThat(examplesTable.asString(), equalTo(FILTERED_TABLE_WITH_INLINED_SEPARATTORS));
    }

    @Test
    void shouldLoadAndProperlyApplyTransformersForExamplesTableFromResourceInput() {
        // Given
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        String lineFromFirstOuterTransformer = "|one|two|";
        String lineFromSecondOuterTransformer = "\n|11|12|";
        String lineFromFirstInnerTransformer = "\n|13|14|";
        String lineFromSecondInnerTransformer = "\n|15|16|\n";
        TableTransformers tableTransformers = new TableTransformers();
        tableTransformers.useTransformer("CUSTOM_TRANSFORMER1", (input, parser, props) ->
                input + lineFromFirstOuterTransformer);
        tableTransformers.useTransformer("CUSTOM_TRANSFORMER2", (input, parser, props) ->
                input + lineFromSecondOuterTransformer);
        tableTransformers.useTransformer("CUSTOM_TRANSFORMER3", (input, parser, props) ->
                input + lineFromFirstInnerTransformer);
        tableTransformers.useTransformer("CUSTOM_TRANSFORMER4", (input, parser, props) ->
                input + lineFromSecondInnerTransformer);
        ExamplesTableFactory factory = new ExamplesTableFactory(resourceLoader, tableTransformers);
        String outerTransformers = "{transformer=CUSTOM_TRANSFORMER1}\n{transformer=CUSTOM_TRANSFORMER2}\n";
        String innerTransformers = "{transformer=CUSTOM_TRANSFORMER3}\n{transformer=CUSTOM_TRANSFORMER4}\n";

        // When
        when(resourceLoader.loadResourceAsText(RESOURCE_PATH)).thenReturn(outerTransformers);
        ExamplesTable examplesTable = factory.createExamplesTable(innerTransformers + RESOURCE_PATH);

        // Then
        assertThat(examplesTable.asString(), equalTo(
                          lineFromFirstOuterTransformer
                        + lineFromSecondOuterTransformer
                        + lineFromFirstInnerTransformer
                        + lineFromSecondInnerTransformer));
    }

    @ValueSource(strings = {
        "headerSeparator",
        "valueSeparator",
        "ignorableSeparator"
    })
    @ParameterizedTest
    void shouldFailIfExamplesTableParsingSeparatorsArePlacesOutsideOfExternalTableTheyBelongTo(String separator) {
        // Given
        ExamplesTableFactory factory = new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers());

        // When
        String tableAsString = String.format("{%s=!}\ndata.table", separator);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> factory.createExamplesTable(tableAsString));

        // Then
        String message = String.format("Examples table parsing separators are not allowed to be applied outside of "
                + "external table they belong to:%n{%s=!}\ndata.table", separator);
        assertEquals(message, thrown.getMessage());
    }

    @Test
    void shouldLoadTableFromPathAndPreserveSeparators() {
        // Given
        ExamplesTableFactory factory = new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers());

        // When
        ExamplesTable table = factory.createExamplesTable("{transformer=FORMATTING}\ndata.table");

        // Then
        assertThat(table.getHeaders(), equalTo(asList("symbol", "threshold", "price", "status")));
        List<Map<String, String>> rows = table.getRows();
        assertThat(rows.size(), equalTo(3));
        ensureRowContentIs(rows, 0, asList("STK1", "15.0", "5.0", "OFF"));
        ensureRowContentIs(rows, 1, asList("STK1", "15.0", "10.0", "OFF"));
        ensureRowContentIs(rows, 2, asList("STK1", "15.0", "16.0", "ON"));
    }

    @Test
    void shouldLoadTableFromAnotherTableContainingTableReference() {
        // Given
        ExamplesTableFactory factory = new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers());

        // When
        ExamplesTable table = factory.createExamplesTable("{transformer=REPLACING, replacing=STK1, replacement=STK2}"
                + System.lineSeparator() + "{transformer=REPLACING, replacing=price, replacement=cost}"
                + System.lineSeparator() + "data-reference.table");

        // Then
        assertThat(table.getHeaders(), equalTo(asList("symbol", "threshold", "cost", "status")));
        List<Map<String, String>> rows = table.getRows();
        assertThat(rows.size(), equalTo(3));
        ensureRowContentIs(rows, 0, asList("STK2", "17.0", "5.0", "OFF"));
        ensureRowContentIs(rows, 1, asList("STK2", "17.0", "10.0", "OFF"));
        ensureRowContentIs(rows, 2, asList("STK2", "17.0", "16.0", "ON"));
    }

    private void ensureRowContentIs(List<Map<String, String>> rows, int row, List<String> expected) {
        assertThat(new ArrayList<>(rows.get(row).values()), equalTo(expected));
    }
}
