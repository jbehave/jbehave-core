package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableTransformers.InvalidTransformationResultException;
import org.jbehave.core.model.TableTransformers.ResolvingSelfReferences;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.jbehave.core.model.TableTransformers.TransformerNotFountException;
import org.jbehave.core.steps.ParameterControls;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TableTransformersBehaviour {

    private static final TableParsers TABLE_PARSERS = new TableParsers(null, null);
    private static final TableProperties PROPERTIES = createExamplesTableProperties();

    private static final String TABLE_AS_STRING = "|one|two|\n"
            + "|11|12|\n"
            + "|21|22|\n";


    private final TableTransformers tableTransformers = new TableTransformers();
    private final ResolvingSelfReferences resolvingSelfReferencesTransformer = new ResolvingSelfReferences(
            new ParameterControls());

    private static TableProperties createExamplesTableProperties() {
        return new TableProperties("", new LocalizedKeywords(), null);
    }

    @Test
    void shouldNotTransformTableIfTransformerNotFound() {
        TransformerNotFountException thrown = assertThrows(TransformerNotFountException.class,
            () -> tableTransformers.transform("inexistentTransformer", TABLE_AS_STRING, TABLE_PARSERS, PROPERTIES));
        assertEquals("Table transformer 'inexistentTransformer' does not exist", thrown.getMessage());
    }

    @Test
    void shouldThrowExceptionIfTableTransformationResultIsNull() {
        TableTransformer transformer = mock();
        when(transformer.transform(TABLE_AS_STRING, TABLE_PARSERS, PROPERTIES)).thenReturn(null);
        String transformerName = "invalid";
        tableTransformers.useTransformer(transformerName, transformer);
        InvalidTransformationResultException thrown = assertThrows(InvalidTransformationResultException.class,
                () -> tableTransformers.transform(transformerName, TABLE_AS_STRING, TABLE_PARSERS, PROPERTIES));
        assertEquals("Table transformation using transformer 'invalid' resulted in 'null'", thrown.getMessage());
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

    @ParameterizedTest
    @CsvSource({
        "'|A|B|C|\n|a|<A>|c|',                     '|A|B|C|\n|a|a|c|\n'",
        "'|A|B|C|\n|a1|<A>|c1|\n|a2|<A>|c2|',      '|A|B|C|\n|a1|a1|c1|\n|a2|a2|c2|\n'",
        "'|A|B|C|D|E|F|\n|<C>|<A>|c|<F>|<D>|<B>|', '|A|B|C|D|E|F|\n|c|c|c|c|c|c|\n'",
        "'|A|B|C|\n|a|<A><C>|c|',                  '|A|B|C|\n|a|ac|c|\n'",
        "'|A|B|C|\n|a<p>|<p><A>|c|',               '|A|B|C|\n|a<p>|<p>a<p>|c|\n'",
        "'|A|B|C|\n|a|<A>|',                       '|A|B|C|\n|a|a||\n'",
        "'|A|B|\n|a|<A>|c|',                       '|A|B|\n|a|a|\n'",
        "'|A|B|C|\n|a|<<A>>|c|',                   '|A|B|C|\n|a|<<A>>|c|\n'"
    })
    void shouldTransform(String beforeTransform, String expectedResult) {
        String transformed = resolvingSelfReferencesTransformer.transform(beforeTransform, TABLE_PARSERS, PROPERTIES);
        assertEquals(expectedResult, transformed);
    }

    @ParameterizedTest
    @CsvSource({
        "'|A|B|C|\n|<B>|<C>|<A>|', A -> B -> C -> A",
        "'|A|B|C|\n|<B>|<C>|<B>|', B -> C -> B"
    })
    void shouldFailWhenSelfReferenceIsDetected(String input, String chain) {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                resolvingSelfReferencesTransformer.transform(input, TABLE_PARSERS, PROPERTIES));
        assertEquals("Circular chain of references is found: " + chain, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "'|A|\n|<A>|'",
        "'|A|\n|a<A>b|'"
    })
    void shouldFailWhenChainOfReferencesIsDetected(String input) {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                resolvingSelfReferencesTransformer.transform(input, TABLE_PARSERS, PROPERTIES));
        assertEquals("Circular self reference is found in column 'A'", exception.getMessage());
    }
}
