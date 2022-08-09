package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.junit.jupiter.api.Test;

class TableParsersBehaviour {

    private static final String KEY_1 = "key-1";
    private static final String KEY_2 = "key-2";

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
                     + "|val-4-1||\n"
                     + "|null|not-null|";

        TableProperties tableProperties = new TableProperties(
                "headerSeparator=!,valueSeparator=|,ignorableSeparator=|--,nullPlaceholder=null",
                new LocalizedKeywords(), null);

        // When
        TableRows tableRows = new TableParsers(null, null).parseRows(table, tableProperties);

        // Then
        List<List<String>> rows = tableRows.getRows();
        assertThat(tableRows.getHeaders(), equalTo(Arrays.asList(KEY_1, KEY_2)));
        assertThat(rows, hasSize(6));
        assertAll(
            () -> assertRow(rows.get(0), "val-1-1", "val-1-2"),
            () -> assertRow(rows.get(1), "val-2-1", "val-2-2"),
            () -> assertRow(rows.get(2), "", ""),
            () -> assertRow(rows.get(3), "", "val-3-2"),
            () -> assertRow(rows.get(4), "val-4-1", ""),
            () -> assertRow(rows.get(5), null, "not-null")
        );
    }

    @Test
    void shouldParseTableWithDefaultNullSeparator() {
        // Given
        String table = "|key-1  |key-2  |\n"
                + "|NULL     |null    |\n"
                + "|NULLABLE|nonNULL|\n";

        TableProperties tableProperties = new TableProperties("", new LocalizedKeywords(), null);

        // When
        TableRows tableRows = new TableParsers(null, null, Optional.of("NULL")).parseRows(table, tableProperties);

        // Then
        List<List<String>> rows = tableRows.getRows();
        assertThat(tableRows.getHeaders(), equalTo(Arrays.asList(KEY_1, KEY_2)));
        assertThat(rows, hasSize(2));
        assertAll(
            () -> assertRow(rows.get(0), null, "null"),
            () -> assertRow(rows.get(1), "NULLABLE", "nonNULL")
        );
    }

    private void assertRow(List<String> row, String cell1, String cell2) {
        assertThat(row, equalTo(Arrays.asList(cell1, cell2)));
    }
}
