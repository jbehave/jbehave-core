package org.jbehave.scenario.definition;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

public class ScenarioDefinition {

    private final String title;
	private final List<String> givenScenarios;
    private final List<String> steps;
	private final ExamplesTable table;

    public ScenarioDefinition(String title) {
        this("", new ArrayList<String>());
    }

	public ScenarioDefinition(List<String> steps) {
        this("", steps);
    }

    public ScenarioDefinition(String title, List<String> steps) {
        this(title, new ArrayList<String>(), new ExamplesTable(""), steps);
    }

    public ScenarioDefinition(String title, List<String> givenScenarios, List<String> steps) {
        this(title, givenScenarios, new ExamplesTable(""), steps);
    }

    public ScenarioDefinition(String title, List<String> givenScenarios, ExamplesTable table, List<String> steps) {
    	this.title = title;
		this.givenScenarios = givenScenarios;
    	this.steps = steps;
		this.table = table;
    }

    public ScenarioDefinition(String title, ExamplesTable table, String... steps) {
        this(title, new ArrayList<String>(), table, asList(steps));
    }
    
    public List<String> getGivenScenarios() {
		return givenScenarios;
	}

	public List<String> getSteps() {
        return steps;
    }

    public String getTitle() {
        return title;
    }

    public ExamplesTable getTable(){
    	return table;
    }
    
}
