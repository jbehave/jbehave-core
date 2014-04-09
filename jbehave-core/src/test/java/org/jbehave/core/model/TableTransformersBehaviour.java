package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Properties;

import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.junit.Test;

public class TableTransformersBehaviour {

    private String tableAsString = "|one|two|\n" + "|11|12|\n" + "|21|22|\n";

    private String landscapeTableAsString = "|one|11|21|\n" + "|two|12|22|\n";

    private String unformattedTableAsString = "|one |   two |   three   |\n" + "|11 | 12 |  1333| \n" + "|21 |22 | 2333  | \n";

    private String formattedTableAsString = "|one|two|three|\n" + "|11 |12 |1333 |\n" + "|21 |22 |2333 |\n";

    private String myTransformedTableAsString = "|one |two | |\n" + "|11 |12 | |\n" + "| 21| 22| |\n";

    @Test
    public void shouldNotTransformTableIfTransformerNotFound() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform("inexistentTransformer", tableAsString, new Properties());
        assertThat(transformed, equalTo(tableAsString));
    }

    @Test
    public void shouldTransformTableFromLandscape() {
        TableTransformers tableTransformers = new TableTransformers();
        String transformed = tableTransformers.transform(TableTransformers.FROM_LANDSCAPE, landscapeTableAsString, new Properties());
        assertThat(transformed, equalTo(tableAsString));
    }


    @Test
    public void shouldTransformTableByFormatting() {
        TableTransformers tableTransformers = new TableTransformers();
		String transformed = tableTransformers.transform(TableTransformers.FORMATTING, unformattedTableAsString, new Properties());
        assertThat(transformed, equalTo(formattedTableAsString));
    }

    @Test
    public void shouldTransformTableWithCustomTransformer() {
        TableTransformers tableTransformers = new TableTransformers();
        tableTransformers.useTransformer("myTransformer", new TableTransformer(){

            public String transform(String tableAsString, Properties properties) {
                return myTransformedTableAsString;
            }
            
        });
        String transformed = tableTransformers.transform("myTransformer", tableAsString, new Properties());
        assertThat(transformed, equalTo(myTransformedTableAsString));
    }

  
}
