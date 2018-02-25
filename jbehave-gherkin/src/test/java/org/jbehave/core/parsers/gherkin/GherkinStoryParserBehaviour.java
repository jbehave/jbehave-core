package org.jbehave.core.parsers.gherkin;

import java.io.IOException;
import java.util.List;

import org.jbehave.core.annotations.Scope;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.StoryParser;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import static org.junit.Assert.assertThat;

public class GherkinStoryParserBehaviour {

	private StoryParser storyParser = new GherkinStoryParser();
	
	@Test
	public void shouldParseStoryWithTabularParameter() throws IOException{
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
	public void shouldParseStoryWithExamples() throws IOException{
		String storyAsText = "Feature: Hello Car\n"
		            + "@scenarioOutline\n"
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
		assertThat(scenario.getMeta().hasProperty("scenarioOutline"), is(true));
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
	public void shouldParseStoryWithNarrative() throws IOException{
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

	@Test
	public void shouldParseStoryWithAlternativeNarrative() throws IOException{
		String storyAsText = "Feature: Hello Car\n"
				    + "Narrative:\n"
				    + "As a car driver\n"
				    + "I want to drive cars on 4 wheels\n"
				    + "So that I can feel safer\n"
					+ "Scenario: Car can drive\n"
					+ "Given I have a car with 4 wheels\n"
					+ "Then I can drive it.\n";
		Story story = storyParser.parseStory(storyAsText);
        assertThat(story.getDescription().asString(), equalTo("Hello Car"));
        Narrative narrative = story.getNarrative();
        assertThat(narrative.asA(), equalTo("car driver"));
        assertThat(narrative.iWantTo(), equalTo("drive cars on 4 wheels"));
        assertThat(narrative.soThat(), equalTo("I can feel safer"));
	}

    @Test
    public void shouldParseStoryWithBackground() throws IOException{
        String storyAsText = "Feature: Hello Car\n\n"
                    + "Background:\n"
                    + "Given I have a license\n\n"
                    + "Scenario: Car can drive\n"
                    + "Given I have a car with 4 wheels\n"
                    + "Then I can drive it.\n";
        Story story = storyParser.parseStory(storyAsText);
        assertThat(story.getDescription().asString(), equalTo("Hello Car"));
        assertThat(story.getLifecycle().getBeforeSteps(Scope.SCENARIO), hasItem("Given I have a license"));
        assertThat(story.getScenarios().get(0).getSteps().size(), equalTo(2));
    }

    @Test
    public void shouldParseStoryWithTags() throws IOException{
        String storyAsText = "@feature\n"
                    + "Feature: Hello Car\n\n"
                    + "Background:\n"
                    + "Given I have a license\n\n"
                    + "@scenario\n"
                    + "Scenario: Car can drive\n"
                    + "Given I have a car with 4 wheels\n"
                    + "Then I can drive it.\n";
        Story story = storyParser.parseStory(storyAsText);
        assertThat(story.getDescription().asString(), equalTo("Hello Car"));
        assertThat(story.getMeta().hasProperty("feature"), is(true));        
        Scenario scenario = story.getScenarios().get(0);
        assertThat(scenario.getSteps().size(), equalTo(2));
        assertThat(scenario.getMeta().hasProperty("scenario"), is(true));        
    }

}
