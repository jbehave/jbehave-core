package org.jbehave.core.parsers.gherkin;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.StoryParser;
import org.junit.Test;

public class GherkinStoryParserBehaviour {

	private StoryParser storyParser = new GherkinStoryParser();
	
	@Test
	public void shouldParseStoryWithTabularParameterUsingGherkin() throws IOException{
		String storyAsText = "Feature: Hello Car\n"
					+ "Scenario: Car can drive\n"
					+ "Given I have a car\n"
					+ "Then I can drive them according to:\n"
					+ "| wheels | can_drive |\n"
					+ "| 1 | false |\n"
					+ "| 2 | false |\n"
					+ "| 3 | false |\n"
					+ "| 4 | true |\n";
		Story story = storyParser.parseStory(storyAsText);
		assertThat(story.getDescription().asString(), equalTo("Hello Car"));		
		List<Scenario> scenarios = story.getScenarios();
		assertThat(scenarios.size(), equalTo(1));
		Scenario scenario = scenarios.get(0);
		List<String> steps = scenario.getSteps();
		assertThat(scenario.getTitle(), equalTo("Car can drive"));		
		assertThat(steps.size(), equalTo(2));
		assertThat(steps.get(0), equalTo("Given I have a car"));
		assertThat(steps.get(1), equalTo("Then I can drive them according to:\n"
				+ "|wheels|can_drive|\n"
				+ "|1|false|\n"
				+ "|2|false|\n"
				+ "|3|false|\n"
				+ "|4|true|"));
	}

	@Test
	public void shouldParseStoryWithExamplesUsingGherkin() throws IOException{
		String storyAsText = "Feature: Hello Car\n"
					+ "Scenario Outline: Car can drive\n"
					+ "Given I have a car\n"
					+ "When I add <wheels>\n"
					+ "Then It <can_drive>\n"
					+ "\n"
					+ "Examples:\n"
					+ "| wheels | can_drive |\n"
					+ "| 1 | false |\n"
					+ "| 2 | false |\n"
					+ "| 3 | false |\n"
					+ "| 4 | true |";
		Story story = storyParser.parseStory(storyAsText);		
		assertThat(story.getDescription().asString(), equalTo("Hello Car"));		
		List<Scenario> scenarios = story.getScenarios();
		assertThat(scenarios.size(), equalTo(1));
		Scenario scenario = scenarios.get(0);
		List<String> steps = scenario.getSteps();
		assertThat(scenario.getTitle(), equalTo("Car can drive"));		
		assertThat(steps.size(), equalTo(3));
		assertThat(steps.get(0), equalTo("Given I have a car"));
		assertThat(steps.get(1), equalTo("When I add <wheels>"));
		assertThat(steps.get(2), equalTo("Then It <can_drive>"));
		assertThat(scenario.getExamplesTable().asString(), equalTo(
				  "|wheels|can_drive|\n"
				+ "|1|false|\n"
				+ "|2|false|\n"
				+ "|3|false|\n"
				+ "|4|true|\n"));
	}
	
	@Test
	public void shouldParseStoryWithNarrativeUsingGherkin() throws IOException{
		String storyAsText = "Feature: Hello Car\n"
				    + "Narrative:\n"
				    + "In order to feel safer\n"
				    + "As a car driver\n"
				    + "I want to drive cars on 4 wheels\n"
					+ "Scenario: Car can drive\n"
					+ "Given I have a car with 4 wheels\n"
					+ "Then I can drive it.\n";
		Story story = storyParser.parseStory(storyAsText);
		assertThat(story.getDescription().asString(), equalTo("Hello Car"));		
		Narrative narrative = story.getNarrative();
		assertThat(narrative.inOrderTo(), equalTo("feel safer"));
		assertThat(narrative.asA(), equalTo("car driver"));
		assertThat(narrative.iWantTo(), equalTo("drive cars on 4 wheels"));
	}

}
