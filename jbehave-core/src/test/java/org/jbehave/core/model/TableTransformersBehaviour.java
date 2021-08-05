package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.junit.jupiter.api.Test;

class TableTransformersBehaviour {

    private static final TableParsers TABLE_PARSERS = new TableParsers(null, null);
    private static final TableProperties PROPERTIES = createExamplesTableProperties();

    private static final String TABLE_AS_STRING = "|one|two|\n"
            + "|11|12|\n"
            + "|21|22|\n";


    private final TableTransformers tableTransformers = new TableTransformers();

    private static TableProperties createExamplesTableProperties() {
        return new TableProperties("", new LocalizedKeywords(), null);
    }

    @Test
    void shouldNotTransformTableIfTransformerNotFound() {
        String transformed = tableTransformers.transform("inexistentTransformer", TABLE_AS_STRING, TABLE_PARSERS,
                PROPERTIES);
        assertThat(transformed, equalTo(TABLE_AS_STRING));
    }

    @Test
    void shouldTransformTableFromLandscape() {
        String landscapeTableAsString = "|one|11|21|\n"
                + "|two|12|22|\n";
        String transformed = tableTransformers.transform(TableTransformers.FROM_LANDSCAPE, landscapeTableAsString,
                TABLE_PARSERS, PROPERTIES);
        assertThat(transformed, equalTo(TABLE_AS_STRING));
    }

    @Test
    void shouldThrowErrorAtTransformingJaggedTableFromLandscape() {
        String landscapeTableAsString = "|one|1|\n"
                + "|two  |2|\n"
                + "|three|3|4|\n";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tableTransformers.transform(TableTransformers.FROM_LANDSCAPE, landscapeTableAsString,
                        TABLE_PARSERS, PROPERTIES));
        assertThat(exception.getMessage(), equalTo("The table rows have unequal numbers of cells: "
                + "one -> 1 cell, two -> 1 cell, three -> 2 cells"));
    }

    @Test
    void shouldTransformTableByFormatting() {
        String unformattedTableAsString = "|one |   two |   three   |\n"
                + "|11 | 12 |  1333| \n"
                + "|21 |22 | 2333  | \n";
        String formattedTableAsString = "|one|two|three|\n"
                + "|11 |12 |1333 |\n"
                + "|21 |22 |2333 |\n";
        String transformed = tableTransformers.transform(TableTransformers.FORMATTING, unformattedTableAsString,
                TABLE_PARSERS, PROPERTIES);
        assertThat(transformed, equalTo(formattedTableAsString));
    }

    @Test
    void shouldTransformTableByReplacement() {
        TableProperties properties = createExamplesTableProperties();
        properties.getProperties().setProperty("replacing", "|");
        properties.getProperties().setProperty("replacement", "\t");
        String transformed = tableTransformers.transform(TableTransformers.REPLACING, TABLE_AS_STRING, TABLE_PARSERS,
                properties);
        assertThat(transformed, equalTo(TABLE_AS_STRING.replace("|", "\t")));
    }

    @Test
    void shouldTransformNotTableByReplacementIfPropertiesNotFound() {
        String transformed = tableTransformers.transform(TableTransformers.REPLACING, TABLE_AS_STRING, TABLE_PARSERS,
                PROPERTIES);
        assertThat(transformed, equalTo(TABLE_AS_STRING));
    }

    @Test
    void shouldTransformTableWithCustomTransformer() {
        String myTransformedTableAsString = "|one |two | |\n"
                + "|11 |12 | |\n"
                + "| 21| 22| |\n";
        tableTransformers.useTransformer("myTransformer",
                (tableAsString, tableParsers, properties) -> myTransformedTableAsString);
        String transformed = tableTransformers.transform("myTransformer", TABLE_AS_STRING, TABLE_PARSERS, PROPERTIES);
        assertThat(transformed, equalTo(myTransformedTableAsString));
    }
}
