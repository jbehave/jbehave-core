package org.jbehave.examples.core.steps;

import java.util.Properties;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.TableTransformers;

public class TableSteps {

	private TableTransformers transformers = new TableTransformers();
	private String table;

	@Given("the table: %table")
	public void givenTheTable(String table) {
		this.table = table;
	}

	@Then("the table transformed by %transformer is: %table")
	public void thenTheTransformedTableIs(String transformer, String table) {
		String transformed = this.transformers.transform(transformer, this.table, new Properties());
		MatcherAssert.assertThat(transformed.trim(), Matchers.equalTo(table));
	}
	
}

