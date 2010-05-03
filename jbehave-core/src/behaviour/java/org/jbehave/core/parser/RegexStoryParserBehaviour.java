package org.jbehave.core.parser;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.jbehave.Ensure.ensureThat;

import java.util.List;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.Ignore;
import org.junit.Test;


public class RegexStoryParserBehaviour {

    private static final String NL = "\n";
    private StoryParser parser = new RegexStoryParser(new LocalizedKeywords());
    private String storyPath = "path/to/my.story";

    @Test
    public void shouldParseStoryWithSimpleSteps() {
        String wholeStory = "Given a scenario" + NL +
                "!-- ignore me" + NL +
                "When I parse it" + NL +
                "Then I should get steps";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        ensureThat(story.getPath(), equalTo(storyPath));
        List<String> steps = story.getScenarios().get(0).getSteps();
        ensureThat(steps.get(0), equalTo("Given a scenario"));
        ensureThat(steps.get(1), equalTo("!-- ignore me"));
        ensureThat(steps.get(2), equalTo("When I parse it"));
        ensureThat(steps.get(3), equalTo("Then I should get steps"));
    }

    @Test
    public void shouldParseStoryWithStepsContainingKeywordsAsPartOfTheContent() {
        String wholeStory = "Given a scenario Givenly" + NL +
                "When I parse it to Whenever" + NL +
                "And I parse it to Anderson" + NL +
                "!-- ignore me too" + NL +
                "Then I should get steps Thenact";
        Story story = parser.parseStory(
                wholeStory, storyPath);

        List<String> steps = story.getScenarios().get(0).getSteps();
        ensureThat(steps.get(0), equalTo("Given a scenario Givenly"));
        ensureThat(steps.get(1), equalTo("When I parse it to Whenever"));
        ensureThat(steps.get(2), equalTo("And I parse it to Anderson"));
        ensureThat(steps.get(3), equalTo("!-- ignore me too"));
        ensureThat(steps.get(4), equalTo("Then I should get steps Thenact"));
    }

    @Test
    public void shouldParseStoryWithMultilineSteps() {
        String wholeStory = "Given a scenario" + NL +
                "with this line" + NL +
                "When I parse it" + NL +
                "with another line" + NL + NL +
                "Then I should get steps" + NL +
                "without worrying about lines" + NL +
                "or extra white space between or after steps" + NL + NL;
        Story story = parser.parseStory(wholeStory, storyPath);

        List<String> steps = story.getScenarios().get(0).getSteps();

        ensureThat(steps.get(0), equalTo("Given a scenario" + NL +
                "with this line"));
        ensureThat(steps.get(1), equalTo("When I parse it" + NL +
                "with another line"));
        ensureThat(steps.get(2), equalTo("Then I should get steps" + NL +
                "without worrying about lines" + NL +
                "or extra white space between or after steps"));
    }

    @Test
    public void shouldExtractStoryWithMultilineScenarioTitle() {
        String wholeStory = "Scenario: A title\n that is spread across\n multiple lines" + NL + NL +
                "Given a step that's pending" + NL +
                "When I run the scenario" + NL +
                "Then I should see this in the output";

        Story story = parser.parseStory(wholeStory, null);

        ensureThat(story.getScenarios().get(0).getTitle(), equalTo("A title\n that is spread across\n multiple lines"));
    }

    @Test
    public void shouldParseWithMultipleScenarios() {
        String wholeStory = "Scenario: the first scenario " + NL + NL +
                "Given my scenario" + NL + NL +
                "Scenario: the second scenario" + NL + NL +
                "Given my second scenario";
        Story story = parser.parseStory(wholeStory, storyPath);

        ensureThat(story.getScenarios().get(0).getTitle(), equalTo("the first scenario"));
        ensureThat(story.getScenarios().get(0).getSteps(), equalTo(asList("Given my scenario")));
        ensureThat(story.getScenarios().get(1).getTitle(), equalTo("the second scenario"));
        ensureThat(story.getScenarios().get(1).getSteps(), equalTo(asList("Given my second scenario")));
    }

    @Test
    public void shouldParseStoryWithNarrative() {
        String wholeStory = "Narrative: This bit of text is ignored" + NL +
                "In order to renovate my house" + NL +
                "As a customer" + NL +
                "I want to get a loan" + NL +
                "Scenario:  A first scenario";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        Narrative narrative = story.getNarrative();
        ensureThat(narrative, not(equalTo(Narrative.EMPTY)));
        ensureThat(narrative.inOrderTo().toString(), equalTo("renovate my house"));
        ensureThat(narrative.asA().toString(), equalTo("customer"));
        ensureThat(narrative.iWantTo().toString(), equalTo("get a loan"));
    }

    @Test
    public void shouldParseStoryWithAllElements() {
        String wholeStory = "This is just a story description" + NL + NL +

                "Narrative: " + NL +
                "In order to see what we're not delivering" + NL + NL +
                "As a developer" + NL +
                "I want to see the narrative for my story when a scenario in that story breaks" + NL +

                "Scenario: A pending scenario" + NL + NL +
                "Given a step that's pending" + NL +
                "When I run the scenario" + NL +
                "!-- A comment between steps" + NL +
                "Then I should see this in the output" + NL +

                "Scenario: A passing scenario" + NL +
                "Given I'm not reporting passing stories" + NL +
                "When I run the scenario" + NL +
                "Then this should not be in the output" + NL +

                "Scenario: A failing scenario" + NL +
                "Given a step that fails" + NL +
                "When I run the scenario" + NL +
                "Then I should see this in the output" + NL +
                "And I should see this in the output" + NL;

        Story story = parser.parseStory(wholeStory, storyPath);

        ensureThat(story.getDescription().asString(), equalTo("This is just a story description"));

        ensureThat(story.getNarrative().inOrderTo(), equalTo("see what we're not delivering"));
        ensureThat(story.getNarrative().asA(), equalTo("developer"));
        ensureThat(story.getNarrative().iWantTo(), equalTo("see the narrative for my story when a scenario in that story breaks"));

        ensureThat(story.getScenarios().get(0).getTitle(), equalTo("A pending scenario"));
        ensureThat(story.getScenarios().get(0).getGivenStoryPaths().size(), equalTo(0));
        ensureThat(story.getScenarios().get(0).getSteps(), equalTo(asList(
                "Given a step that's pending",
                "When I run the scenario",
                "!-- A comment between steps",
                "Then I should see this in the output"
        )));

        ensureThat(story.getScenarios().get(1).getTitle(), equalTo("A passing scenario"));
        ensureThat(story.getScenarios().get(1).getGivenStoryPaths().size(), equalTo(0));
        ensureThat(story.getScenarios().get(1).getSteps(), equalTo(asList(
                "Given I'm not reporting passing stories",
                "When I run the scenario",
                "Then this should not be in the output"
        )));

        ensureThat(story.getScenarios().get(2).getTitle(), equalTo("A failing scenario"));
        ensureThat(story.getScenarios().get(2).getGivenStoryPaths().size(), equalTo(0));
        ensureThat(story.getScenarios().get(2).getSteps(), equalTo(asList(
                "Given a step that fails",
                "When I run the scenario",
                "Then I should see this in the output",
                "And I should see this in the output"
        )));
    }

    @Test
    public void shouldParseLongStoryWithKeywordSplitScenarios() {
        ensureLongStoryCanBeParsed(parser);
    }

    @Test
    @Ignore("It should fail due to regex stack overflow")
    public void shouldParseLongStoryWithPatternSplitScenarios() {
        StoryParser parser = new RegexStoryParser(new LocalizedKeywords()) {

            @Override
            protected List<String> splitScenarios(String storyAsText) {
                return super.splitScenariosWithPattern(storyAsText);
            }

        };
        ensureLongStoryCanBeParsed(parser);
    }

    private void ensureLongStoryCanBeParsed(StoryParser parser) {
        String aGivenWhenThen =
                "Given a step" + NL +
                        "When I run it" + NL +
                        "Then I should seen an output" + NL;

        StringBuffer aScenario = new StringBuffer();

        aScenario.append("Scenario: A long scenario").append(NL);
        int numberOfGivenWhenThensPerScenario = 50;
        for (int i = 0; i < numberOfGivenWhenThensPerScenario; i++) {
            aScenario.append(aGivenWhenThen);
        }

        int numberOfScenarios = 100;
        StringBuffer wholeStory = new StringBuffer();
        wholeStory.append("Story: A very long story").append(NL);
        for (int i = 0; i < numberOfScenarios; i++) {
            wholeStory.append(aScenario).append(NL);
        }

        Story story = parser.parseStory(wholeStory.toString(), null);
        ensureThat(story.getScenarios().size(), equalTo(numberOfScenarios));
        for (Scenario scenario : story.getScenarios()) {
            ensureThat(scenario.getSteps().size(), equalTo(numberOfGivenWhenThensPerScenario * 3));
        }
    }

    @Test
    public void shouldParseStoryWithScenarioContainingExamplesTable() {
        String wholeStory = "Scenario: A scenario with examples table" + NL + NL +
                "Given a step with a <one>" + NL +
                "When I run the scenario of name <two>" + NL +
                "Then I should see <three> in the output" + NL +

                "Examples:" + NL +
                "|one|two|three|" + NL +
                "|11|12|13|" + NL +
                "|21|22|23|";

        Story story = parser.parseStory(wholeStory, storyPath);

        Scenario scenario = story.getScenarios().get(0);
        ensureThat(scenario.getTitle(), equalTo("A scenario with examples table"));
        ensureThat(scenario.getGivenStoryPaths().size(), equalTo(0));
        ensureThat(scenario.getSteps(), equalTo(asList(
                "Given a step with a <one>",
                "When I run the scenario of name <two>",
                "Then I should see <three> in the output"
        )));
        ExamplesTable table = scenario.getTable();
        ensureThat(table.toString(), equalTo(
                "|one|two|three|" + NL +
                        "|11|12|13|" + NL +
                        "|21|22|23|"));
        ensureThat(table.getRowCount(), equalTo(2));
        ensureThat(table.getRow(0), not(nullValue()));
        ensureThat(table.getRow(0).get("one"), equalTo("11"));
        ensureThat(table.getRow(0).get("two"), equalTo("12"));
        ensureThat(table.getRow(0).get("three"), equalTo("13"));
        ensureThat(table.getRow(1), not(nullValue()));
        ensureThat(table.getRow(1).get("one"), equalTo("21"));
        ensureThat(table.getRow(1).get("two"), equalTo("22"));
        ensureThat(table.getRow(1).get("three"), equalTo("23"));
    }

    @Test
    public void shouldParseStoryWithScenarioContainingGivenStories() {
        String wholeStory = "Scenario: A scenario with given stories" + NL + NL +
                "GivenStories: path/to/one,path/to/two" + NL + NL +
                "Given a step" + NL +
                "When I run it" + NL +
                "Then I should an output";

        Story story = parser.parseStory(wholeStory, storyPath);

        Scenario scenario = story.getScenarios().get(0);
        ensureThat(scenario.getTitle(), equalTo("A scenario with given stories"));
        ensureThat(scenario.getGivenStoryPaths(), equalTo(asList(
                "path/to/one",
                "path/to/two")));
        ensureThat(scenario.getSteps(), equalTo(asList(
                "Given a step",
                "When I run it",
                "Then I should an output"
        )));

    }

    @Test
    public void shouldParseStoryWithoutAPath() {
        String wholeStory = "Given a step" + NL +
                "When I run it" + NL +
                "Then I should an output";

        Story story = parser.parseStory(wholeStory);

        ensureThat(story.getPath(), equalTo(""));
        Scenario scenario = story.getScenarios().get(0);
        ensureThat(scenario.getSteps(), equalTo(asList(
                "Given a step",
                "When I run it",
                "Then I should an output"
        )));

    }
}
