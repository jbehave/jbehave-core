package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TableParsersBehaviour {

    private final TableParsers tableParsers = new TableParsers();

    @Test
    void shouldParseTableUsingProperties() {
        // Given
        String table = "!key-1  !key-2  !\n"
                     + "  |--- ignore me \n"
                     + "|val-1-1|val-1-2|\n"
                     + "|--- and me      \n"
                     + "|val-2-1|val-2-2|\n"
                     + "|||\n"
                     + "||val-3-2|\n"
                     + "|val-4-1||";

        Properties properties = new Properties();
        properties.put("headerSeparator", "!");
        properties.put("valueSeparator", "|");
        properties.put("ExamplesTableIgnorableSeparator", "|--");
        TableProperties tableProperties = new TableProperties(properties);

        // When
        TableRows tableRows = tableParsers.parseRows(table, tableProperties);

        // Then
        List<Map<String, String>> rows = tableRows.getRows();
        Assertions.assertAll(
            () -> assertThat(tableRows.getHeaders(), equalTo(Arrays.asList("key-1", "key-2"))),
            () -> assertThat(rows, hasSize(5)),
            () -> assertRow(rows.get(0), "val-1-1", "val-1-2"),
            () -> assertRow(rows.get(1), "val-2-1", "val-2-2"),
            () -> assertRow(rows.get(2), "", ""),
            () -> assertRow(rows.get(3), "", "val-3-2"),
            () -> assertRow(rows.get(4), "val-4-1", "")
         );
    }

    private void assertRow(Map<String, String> row, String cell1, String cell2) {
        Map<String, String> expected = new HashMap<>();
        expected.put("key-1", cell1);
        expected.put("key-2", cell2);
        assertThat(row, equalTo(expected));
    }
}
