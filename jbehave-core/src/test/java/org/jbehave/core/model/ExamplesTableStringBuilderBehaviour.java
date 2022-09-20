package org.jbehave.core.model;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;

class ExamplesTableStringBuilderBehaviour {

    @Test
    void shouldBuildExamplesTableStringFromData() {
        List<String> headers = asList("h1", "h2");
        List<List<String>> rows = asList(
                asList("c11", "c12"),
                asList("c21", "c22")
        );
        String examplesTableString = ExamplesTableStringBuilder.buildExamplesTableString(
                new TableProperties("", new Keywords(), new ParameterConverters()), headers, rows);
        String expected = "|h1|h2|\n"
                + "|c11|c12|\n"
                + "|c21|c22|\n";
        assertEquals(expected, examplesTableString);
    }

    @Test
    void shouldBuildExamplesTableStringFromJaggedData() {
        List<String> headers = asList("k1", "k2");
        List<List<String>> rows = asList(
                asList("v11", "v12"),
                asList("v21"),
                asList("v31", "v32", "v33")
        );
        String examplesTableString = ExamplesTableStringBuilder.buildExamplesTableString(
                new TableProperties("", new Keywords(), new ParameterConverters()), headers, rows);
        String expected = "|k1|k2|\n"
                + "|v11|v12|\n"
                + "|v21||\n"
                + "|v31|v32|\n";
        assertEquals(expected, examplesTableString);
    }
}
