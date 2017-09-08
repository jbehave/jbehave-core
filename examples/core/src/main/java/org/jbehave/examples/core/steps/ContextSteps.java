package org.jbehave.examples.core.steps;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;

import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;


public class ContextSteps {

	private MyContext context;

	public ContextSteps(){
		this(new MyContext());
	}
	
	public ContextSteps(MyContext context) {
		this.context = context;
	}
	
	@When("a variable of name $name is processed")
	public void whenIProcessAVariable(String name){
		context.variables.put(name, name);
	}

	@Then("the context includes the name $name")
	public void thenTheContextIncludes(String name){
		Map<String, Object> variables = context.variables;
		MatcherAssert.assertThat(variables, hasEntry(name, (Object)name));
	}

	@Then("the context does not include the name $name")
	public void thenTheContextDoesNotInclude(String name){
		Map<String, Object> variables = context.variables;
		MatcherAssert.assertThat(variables, not(hasEntry(name, (Object)name)));
	}

	@AfterScenario
	public void afterScenario(){
		context.variables.clear();
	}
	
}
