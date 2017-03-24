package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.junit.Test;

public class TableTransformersBehaviour {

    private static final ExamplesTableProperties PROPERTIES = createExamplesTableProperties();

    private String tableAsString = "|one|two|\n" + "|11|12|\n" + "|21|22|\n";

    private String landscapeTableAsString = "|one|11|21|\n" + "|two|12|22|\n";

    private String unformattedTableAsString = "|one |   two |   three   |\n" + "|11 | 12 |  1333| \n" + "|21 |22 | 2333  | \n";

    private String formattedTableAsString = "|one|two|three|\n" + "|11 |12 |1333 |\n" + "|21 |22 |2333 |\n";

    private String myTransformedTableAsString = "|one |two | |\n" + "|11 |12 | |\n" + "| 21| 22| |\n";

    private static ExamplesTableProperties createExamplesTableProperties() {
        return new ExamplesTableProperties("", "|", "|", "!--");
    }

    @Test
    public void shouldNotTransformTableIfTransformerNotFound() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform("inexistentTransformer", tableAsString, PROPERTIES);
        assertThat(transformed, equalTo(tableAsString));
    }

    @Test
    public void shouldTransformTableFromLandscape() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform(TableTransformers.FROM_LANDSCAPE, landscapeTableAsString,
                PROPERTIES);
        assertThat(transformed, equalTo(tableAsString));
    }


    @Test
    public void shouldTransformTableByFormatting() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform(TableTransformers.FORMATTING, unformattedTableAsString,
                PROPERTIES);
        assertThat(transformed, equalTo(formattedTableAsString));
    }

    @Test
    public void shouldTransformTableByReplacement() {
        TableTransformers tableTransformers = new TableTransformers();
        ExamplesTableProperties properties = createExamplesTableProperties();
        properties.getProperties().setProperty("replacing", "|");
        properties.getProperties().setProperty("replacement", "\t");
        String transformed = tableTransformers.transform(TableTransformers.REPLACING, tableAsString, properties);
        assertThat(transformed, equalTo(tableAsString.replace("|", "\t")));
    }

    @Test
    public void shouldTransformNotTableByReplacementIfPropertiesNotFound() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform(TableTransformers.REPLACING, tableAsString, PROPERTIES);
        assertThat(transformed, equalTo(tableAsString));
    }

    @Test
    public void shouldTransformTableWithCustomTransformer() {
        TableTransformers tableTransformers = new TableTransformers();
        tableTransformers.useTransformer("myTransformer", new TableTransformer(){

            @Override
            public String transform(String tableAsString, ExamplesTableProperties properties) {
                return myTransformedTableAsString;
            }
            
        });
        String transformed = tableTransformers.transform("myTransformer", tableAsString, PROPERTIES);
        assertThat(transformed, equalTo(myTransformedTableAsString));
    }
}
