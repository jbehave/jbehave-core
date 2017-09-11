package org.jbehave.examples.core.steps;

import java.util.Properties;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableProperties;
import org.jbehave.core.model.TableTransformers;

public class TableSteps {

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
		ExamplesTableProperties properties = new ExamplesTableProperties(new ExamplesTable(this.table).getProperties());
		String transformed = this.transformers.transform(transformer, this.table, properties);
		MatcherAssert.assertThat(transformed.trim(), Matchers.equalTo(table));
	}
	
}

