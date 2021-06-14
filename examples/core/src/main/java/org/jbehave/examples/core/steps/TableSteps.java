package org.jbehave.examples.core.steps;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterConverters;

public class TableSteps {

    private final Keywords keywords = new LocalizedKeywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();
    private final TableParsers tableParsers = new TableParsers(keywords, parameterConverters);

    private TableTransformers transformers = new TableTransformers();
    private String table;
    private String type;
    private ExamplesTable examplesTable;

    @Given("the table: $table")
    public void givenTheTable(String table) {
        this.table = table;
    }

    @Given("the table as parameter: $table")
    public void givenTheTableAsParameter(ExamplesTable table) {
        this.examplesTable = table;
    }

    @Given("the table of type $type as parameter: $table")
    public void givenTheTableAsParameter(String type, ExamplesTable table) {
        this.type = type;
        this.examplesTable = table;
    }

    @Then("the table transformed by $transformer is: $table")
    public void thenTheTransformedTableIs(String transformer, String table) {
        TableProperties properties = new TableProperties(new ExamplesTable(this.table).getPropertiesAsString(),
                keywords, parameterConverters);
        String transformed = this.transformers.transform(transformer, this.table, tableParsers, properties);
        MatcherAssert.assertThat(transformed.trim(), Matchers.equalTo(table));
    }
    
}

