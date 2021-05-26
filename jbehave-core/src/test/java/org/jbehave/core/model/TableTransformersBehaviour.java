package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.junit.jupiter.api.Test;

class TableTransformersBehaviour {

    private static final TableProperties PROPERTIES = createExamplesTableProperties();

    private TableParsers tableParsers = new TableParsers(null, null);

    private String tableAsString = "|one|two|\n" + "|11|12|\n" + "|21|22|\n";

    private String landscapeTableAsString = "|one|11|21|\n" + "|two|12|22|\n";

    private String unformattedTableAsString = "|one |   two |   three   |\n" + "|11 | 12 |  1333| \n" + "|21 |22 | 2333  | \n";

    private String formattedTableAsString = "|one|two|three|\n" + "|11 |12 |1333 |\n" + "|21 |22 |2333 |\n";

    private String myTransformedTableAsString = "|one |two | |\n" + "|11 |12 | |\n" + "| 21| 22| |\n";

    private static TableProperties createExamplesTableProperties() {
        return new TableProperties(null, "", "|", "|", "!--");
    }

    @Test
    void shouldNotTransformTableIfTransformerNotFound() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform("inexistentTransformer", tableAsString, tableParsers, PROPERTIES);
        assertThat(transformed, equalTo(tableAsString));
    }

    @Test
    void shouldTransformTableFromLandscape() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform(TableTransformers.FROM_LANDSCAPE, landscapeTableAsString,
                tableParsers, PROPERTIES);
        assertThat(transformed, equalTo(tableAsString));
    }


    @Test
    void shouldTransformTableByFormatting() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform(TableTransformers.FORMATTING, unformattedTableAsString,
                tableParsers, PROPERTIES);
        assertThat(transformed, equalTo(formattedTableAsString));
    }

    @Test
    void shouldTransformTableByReplacement() {
        TableTransformers tableTransformers = new TableTransformers();
        TableProperties properties = createExamplesTableProperties();
        properties.getProperties().setProperty("replacing", "|");
        properties.getProperties().setProperty("replacement", "\t");
        String transformed = tableTransformers.transform(TableTransformers.REPLACING, tableAsString, tableParsers, properties);
        assertThat(transformed, equalTo(tableAsString.replace("|", "\t")));
    }

    @Test
    void shouldTransformNotTableByReplacementIfPropertiesNotFound() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform(TableTransformers.REPLACING, tableAsString, tableParsers, PROPERTIES);
        assertThat(transformed, equalTo(tableAsString));
    }

    @Test
    void shouldTransformTableWithCustomTransformer() {
        TableTransformers tableTransformers = new TableTransformers();
        tableTransformers.useTransformer("myTransformer", new TableTransformer() {

            @Override
            public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties) {
                return myTransformedTableAsString;
            }
            
        });
        String transformed = tableTransformers.transform("myTransformer", tableAsString, tableParsers, PROPERTIES);
        assertThat(transformed, equalTo(myTransformedTableAsString));
    }
}
