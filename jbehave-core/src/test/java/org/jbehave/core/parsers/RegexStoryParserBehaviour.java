package org.jbehave.core.parsers;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.TableTransformers;
import org.junit.jupiter.api.Test;


class RegexStoryParserBehaviour {

    private static final String NL = "\n";
    private StoryParser parser = new RegexStoryParser();
    private String storyPath = "path/to/my.story";

    @Test
    void shouldParseStoryAndProvideNameFromPath() {
        Story story = parser.parseStory(EMPTY, storyPath);
        assertThat(story.getPath(), equalTo(storyPath));
        assertThat(story.getName(), equalTo(new File(storyPath).getName()));
    }

    @Test
    void shouldParseStoryAndProvideEmptyNameWhenPathIsNull() {
        Story story = parser.parseStory(EMPTY, null);
        assertThat(story.getPath(), equalTo(EMPTY));
        assertThat(story.getName(), equalTo(EMPTY));
    }

    @Test
    void shouldParseStoryWithMetaAndGivenStories() {
        String wholeStory = "Meta: @skip @theme parsing" + NL + 
                "GivenStories: path1,path2 " + NL +
                "Scenario: A scenario" + NL +
                "Meta: @author Mauro" + NL +
                "Given a step " + NL +
                "Scenario: Another scenario" + NL +
                "Meta: @author Paul" + NL +
                "Given another step ";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        assertThat(story.getPath(), equalTo(storyPath));
        Meta storyMeta = story.getMeta();
        assertThat(storyMeta.getProperty("theme"), equalTo("parsing"));
        assertThat(storyMeta.getProperty("skip"), equalTo(EMPTY));
        assertThat(storyMeta.getProperty("unknown"), equalTo(EMPTY));
        assertThat(story.getGivenStories().getPaths(), equalTo(asList("path1", "path2")));
        List<Scenario> scenarios = story.getScenarios();
        assertThat(scenarios.get(0).getTitle(), equalTo("A scenario"));
        assertThat(scenarios.get(0).getMeta().getProperty("author"), equalTo("Mauro"));
        assertThat(scenarios.get(1).getTitle(), equalTo("Another scenario"));
        assertThat(scenarios.get(1).getMeta().getProperty("author"), equalTo("Paul"));
    }
    
    @Test
    void shouldParseStoryWithMetaAndLifecycle() {
        String wholeStory = "Meta: @skip @theme parsing" + NL + 
                "Lifecycle:" + NL +
                "Before:" + NL +
                "Given a step before each scenario" + NL + 
                "And another before step" + NL +
                "Scenario: A scenario" + NL +
                "Meta: @author Mauro" + NL +
                "Given a step " + NL +
                "Scenario: Another scenario" + NL +
                "Meta: @author Paul" + NL +
                "Given another step ";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        assertThat(story.getPath(), equalTo(storyPath));
        Meta storyMeta = story.getMeta();
        assertThat(storyMeta.getProperty("theme"), equalTo("parsing"));
        assertThat(storyMeta.getProperty("skip"), equalTo(EMPTY));
        assertThat(storyMeta.getProperty("unknown"), equalTo(EMPTY));
        Lifecycle lifecycle = story.getLifecycle();
        assertThat(lifecycle.getBeforeSteps(Scope.SCENARIO).size(), equalTo(2));
        List<Scenario> scenarios = story.getScenarios();
        assertThat(scenarios.get(0).getTitle(), equalTo("A scenario"));
        assertThat(scenarios.get(0).getMeta().getProperty("author"), equalTo("Mauro"));
        assertThat(scenarios.get(1).getTitle(), equalTo("Another scenario"));
        assertThat(scenarios.get(1).getMeta().getProperty("author"), equalTo("Paul"));
    }

    @Test
    void shouldParseStoryWithMetaAndNarrative() {
        String wholeStory = "Meta: @skip @theme parsing" + NL + 
                "Narrative: This bit of text is ignored" + NL +
                "In order to renovate my house" + NL +
                "As a customer" + NL +
                "I want to get a loan" + NL +
                "Scenario: A scenario" + NL +
                "Meta: @author Mauro" + NL +
                "Given a step " + NL +
                "Scenario: Another scenario" + NL +
                "Meta: @author Paul" + NL +
                "Given another step ";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        assertThat(story.getPath(), equalTo(storyPath));
        Meta storyMeta = story.getMeta();
        assertThat(storyMeta.getProperty("theme"), equalTo("parsing"));
        assertThat(storyMeta.getProperty("skip"), equalTo(EMPTY));
        assertThat(storyMeta.getProperty("unknown"), equalTo(EMPTY));
        Narrative narrative = story.getNarrative();
        assertThat(narrative.isEmpty(), not(true));
        List<Scenario> scenarios = story.getScenarios();
        assertThat(scenarios.get(0).getTitle(), equalTo("A scenario"));
        assertThat(scenarios.get(0).getMeta().getProperty("author"), equalTo("Mauro"));
        assertThat(scenarios.get(1).getTitle(), equalTo("Another scenario"));
        assertThat(scenarios.get(1).getMeta().getProperty("author"), equalTo("Paul"));
    }

    @Test
    void shouldParseStoryWithGivenStoriesWithAnchorParameters() {
        String wholeStory = "GivenStories: path1#{id1:scenario1;id2:scenario2}" + NL +
                "Scenario: A scenario" + NL +
                "Given a step";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        assertThat(story.getPath(), equalTo(storyPath));
        assertThat(story.getGivenStories().getStories().size(), equalTo(1));
        GivenStory givenStory = story.getGivenStories().getStories().get(0);
        assertThat(givenStory.hasAnchorParameters(), equalTo(true));
        Map<String, String> anchorParameters = givenStory.getAnchorParameters();
        assertThat(anchorParameters.size(), equalTo(2));
        assertThat(anchorParameters.get("id1"), equalTo("scenario1"));        
        assertThat(anchorParameters.get("id2"), equalTo("scenario2"));        
    }
    
    @Test
    void shouldAllowSpacesInMetaProperties() {
        String wholeStory = "Meta: @ theme parsing @ skip" + NL +
                "Scenario: " + NL +
                "Meta: @authors Mauro Paul" + NL +
                "Given a scenario " + NL +
                "When I parse it" + NL +
                "Then I should get steps";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        assertThat(story.getPath(), equalTo(storyPath));
        Meta storyMeta = story.getMeta();
        assertThat(storyMeta.getProperty("theme"), equalTo("parsing"));
        assertThat(storyMeta.getProperty("skip"), equalTo(EMPTY));
        assertThat(story.getScenarios().get(0).getMeta().getProperty("authors"), equalTo("Mauro Paul"));
    }

    @Test
    void shouldIgnoreCommentsInMetaProperties() {
        String wholeStory = "Meta: !-- this is the theme @theme parsing !-- skip me @skip" + NL +
                "Scenario: " + NL +
                "Meta: !-- these are the authors @authors Mauro Paul" + NL +
                "Given a scenario " + NL +
                "When I parse it" + NL +
                "Then I should get steps";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        assertThat(story.getPath(), equalTo(storyPath));
        Meta storyMeta = story.getMeta();
        assertThat(storyMeta.getProperty("theme"), equalTo("parsing"));
        assertThat(storyMeta.getProperty("skip"), equalTo(EMPTY));
        assertThat(story.getScenarios().get(0).getMeta().getProperty("authors"), equalTo("Mauro Paul"));
    }

    @Test
    void shouldParseStoryWithSimpleSteps() {
        String wholeStory = "Given a scenario" + NL +
                "!-- ignore me" + NL +
                "When I parse it" + NL +
                "Then I should get steps";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        assertThat(story.getPath(), equalTo(storyPath));
        List<String> steps = story.getScenarios().get(0).getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario"));
        assertThat(steps.get(1), equalTo("!-- ignore me"));
        assertThat(steps.get(2), equalTo("When I parse it"));
        assertThat(steps.get(3), equalTo("Then I should get steps"));
    }

    @Test
    void shouldParseStoryWithStepsContainingKeywordsAtStartOfOtherWords() {
        String wholeStory = "Meta: @some" + NL +
                "Given a scenario Givenly" + NL +
                "When I parse it to Whenever" + NL +
                "And I parse it to Anderson" + NL +
                "!-- ignore me too" + NL +
                "Then I should get steps Thenact";
        Story story = parser.parseStory(
                wholeStory, storyPath);

        List<String> steps = story.getScenarios().get(0).getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario Givenly"));
        assertThat(steps.get(1), equalTo("When I parse it to Whenever"));
        assertThat(steps.get(2), equalTo("And I parse it to Anderson"));
        assertThat(steps.get(3), equalTo("!-- ignore me too"));
        assertThat(steps.get(4), equalTo("Then I should get steps Thenact"));
    }

    @Test
    void shouldParseStoryWithSynonymsOfStartingWords() {
        StoryParser parser = new RegexStoryParser(new LocalizedKeywords(new Locale("sy")));

        String wholeStory = "Given a scenario" + NL +
                "When I parse it" + NL +
                "And I parse it again" + NL +
                "With another parse as well" + NL +
                "!-- ignore me" + NL +
                "Giveth another scenario" + NL +
                "With a merry go round";
        Story story = parser.parseStory(
                wholeStory, storyPath);

        List<String> steps = story.getScenarios().get(0).getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario"));
        assertThat(steps.get(1), equalTo("When I parse it"));
        assertThat(steps.get(2), equalTo("And I parse it again"));
        assertThat(steps.get(3), equalTo("With another parse as well"));
        assertThat(steps.get(4), equalTo("!-- ignore me"));
        assertThat(steps.get(5), equalTo("Giveth another scenario"));
        assertThat(steps.get(6), equalTo("With a merry go round"));
    }

    @Test
    void shouldParseStoryWithGivenStoriesAtStoryAndScenarioLevel() {
        String wholeStory = "GivenStories: GivenAPreconditionToStory" + NL +
                "Scenario:" +NL +
                "GivenStories: GivenAPreconditionToScenario" + NL +
                "Given a scenario Given";
        Story story = parser.parseStory(wholeStory, storyPath);

        assertThat(story.getGivenStories().getPaths(), equalTo(asList("GivenAPreconditionToStory")));
        Scenario scenario = story.getScenarios().get(0);
        assertThat(scenario.getGivenStories().getPaths(), equalTo(asList("GivenAPreconditionToScenario")));
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario Given"));
    }

    @Test
    void shouldParseStoryWithScenarioTitleGivenStoriesAndStepsContainingKeywordsNotAtStartOfLine() {
        String wholeStory = "Scenario: Show that we have Given/When/Then as part of description or step content" +NL +
                "GivenStories: GivenAStoryContainingAKeyword" + NL +
                "Given a scenario Given" + NL +
                "When I parse it to When" + NL +
                "And I parse it to And" + NL +
                "!-- And ignore me too" + NL +
                "Then I should get steps Then" + NL +
                "Examples:" + NL +
                "|Given|When|Then|And|" + NL +
                "|Dato che|Quando|Allora|E|" + NL +
                "|Dado que|Quando|Então|E|" + NL;
        Story story = parser.parseStory(
                wholeStory, storyPath);

        Scenario scenario = story.getScenarios().get(0);
        assertThat(scenario.getTitle(), equalTo("Show that we have Given/When/Then as part of description or step content"));
        assertThat(scenario.getGivenStories().getPaths(), equalTo(asList("GivenAStoryContainingAKeyword")));
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario Given"));
        assertThat(steps.get(1), equalTo("When I parse it to When"));
        assertThat(steps.get(2), equalTo("And I parse it to And"));
        assertThat(steps.get(3), equalTo("!-- And ignore me too"));
        assertThat(steps.get(4), equalTo("Then I should get steps Then"));        
        assertThat(story.getScenarios().get(0).getExamplesTable().asString(), 
                    equalTo("|Given|When|Then|And|" + NL +
                            "|Dato che|Quando|Allora|E|" + NL +
                            "|Dado que|Quando|Então|E|" + NL));
    }
    
    @Test
    void shouldParseStoryWithLifecycle() {
        String wholeStory = "Lifecycle: " + NL +
                "Before:" + NL + NL +
                "Given a step before each scenario" + NL + 
                "And another before step" + NL +
                "After:" + NL + NL +
                "Given a step after each scenario" + NL + 
                "And another after step" + NL +
                "Scenario:" +NL +
                "Given a scenario";
        Story story = parser.parseStory(wholeStory, storyPath);
        List<String> beforeSteps = story.getLifecycle().getBeforeSteps(Scope.SCENARIO);
        assertThat(beforeSteps.get(0), equalTo("Given a step before each scenario"));
        assertThat(beforeSteps.get(1), equalTo("And another before step"));
        List<String> afterSteps = story.getLifecycle().getAfterSteps(Scope.SCENARIO);
        assertThat(afterSteps.get(0), equalTo("Given a step after each scenario"));
        assertThat(afterSteps.get(1), equalTo("And another after step"));
        Scenario scenario = story.getScenarios().get(0);
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario"));
    }

    @Test
    void shouldParseStoryWithLifecycleAndStoryScope() {
        String wholeStory = "Lifecycle: " + NL +
                "Before:" + NL + NL +
                "Scope: STORY" + NL +
                "Given a step before each story" + NL +
                "And another before story" + NL +
                "After:" + NL + NL +
                "Scope: STORY" + NL +
                "Given a step after each story" + NL +
                "And another after story" + NL +
                "Scenario:" +NL +
                "Given a scenario";
        Story story = parser.parseStory(wholeStory, storyPath);
        assertThat(story.getLifecycle().hasBeforeSteps(), is(true));
        List<String> beforeSteps = story.getLifecycle().getBeforeSteps(Scope.STORY);
        assertThat(beforeSteps.get(0), equalTo("Given a step before each story"));
        assertThat(beforeSteps.get(1), equalTo("And another before story"));
        assertThat(story.getLifecycle().getBeforeSteps(Scope.SCENARIO).size(), equalTo(0));
        assertThat(story.getLifecycle().hasAfterSteps(), is(true));
        List<String> afterSteps = story.getLifecycle().getAfterSteps(Scope.STORY);
        assertThat(afterSteps.get(0), equalTo("Given a step after each story"));
        assertThat(afterSteps.get(1), equalTo("And another after story"));
        assertThat(story.getLifecycle().getAfterSteps(Scope.SCENARIO).size(), equalTo(0));
        Scenario scenario = story.getScenarios().get(0);
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario"));
    }

    @Test
    void shouldParseStoryWithLifecycleAndMultipleScopes() {
        String wholeStory = "Lifecycle: " + NL +
                "Before:" + NL + NL +
                "Scope: STEP" + NL +
                "Given a step before each scenario step" + NL +
                "And another before scenario step" + NL +
                "Scope: SCENARIO" + NL +
                "Given a step before each scenario" + NL +
                "And another before scenario" + NL +
                "Scope: STORY" + NL +
                "Given a step before each story" + NL +
                "And another before story" + NL +
                "After:" + NL + NL +
                "Scope: STORY" + NL +
                "Given a step after each story" + NL +
                "And another after story" + NL +
                "Scope: SCENARIO" + NL +
                "Given a step after each scenario" + NL +
                "And another after scenario" + NL +
                "Scope: STEP" + NL +
                "Given a step after each scenario step" + NL +
                "And another after scenario step" + NL +
                "Scenario:" +NL +
                "Given a scenario";
        Story story = parser.parseStory(wholeStory, storyPath);
        Lifecycle lifecycle = story.getLifecycle();
        assertThat(lifecycle.hasBeforeSteps(), is(true));
        assertThat(lifecycle.getBeforeSteps(Scope.STORY),
                equalTo(asList("Given a step before each story", "And another before story")));
        assertThat(lifecycle.getBeforeSteps(Scope.SCENARIO),
                equalTo(asList("Given a step before each scenario", "And another before scenario")));
        assertThat(lifecycle.getBeforeSteps(Scope.STEP),
                equalTo(asList("Given a step before each scenario step", "And another before scenario step")));
        assertThat(lifecycle.hasAfterSteps(), is(true));
        assertThat(lifecycle.getAfterSteps(Scope.STORY),
                equalTo(asList("Given a step after each story", "And another after story")));
        assertThat(lifecycle.getAfterSteps(Scope.SCENARIO),
                equalTo(asList("Given a step after each scenario", "And another after scenario")));
        assertThat(lifecycle.getAfterSteps(Scope.STEP),
                equalTo(asList("Given a step after each scenario step", "And another after scenario step")));
        Scenario scenario = story.getScenarios().get(0);
        assertThat(scenario.getSteps(), equalTo(asList("Given a scenario")));
    }

    @Test
    void shouldParseStoryWithLifecycleBeforeOnly() {
        String wholeStory = "Lifecycle: " + NL +
                "Before:" + NL + NL +
                "Given a step before each scenario" + NL + 
                "And another before step" + NL +
                "Scenario:" +NL +
                "Given a scenario";
        Story story = parser.parseStory(wholeStory, storyPath);
        List<String> beforeSteps = story.getLifecycle().getBeforeSteps(Scope.SCENARIO);
        assertThat(beforeSteps.get(0), equalTo("Given a step before each scenario"));
        assertThat(beforeSteps.get(1), equalTo("And another before step"));
        List<String> afterSteps = story.getLifecycle().getAfterSteps(Scope.SCENARIO);
        assertThat(afterSteps.isEmpty(), equalTo(true));
        Scenario scenario = story.getScenarios().get(0);
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario"));
    }

    @Test
    void shouldParseStoryWithLifecycleAfterOnly() {
        String wholeStory = "Lifecycle: " + NL +
                "After:" + NL + NL +
                "Given a step after each scenario" + NL + 
                "And another after step" + NL +
                "Scenario:" +NL +
                "Given a scenario";
        Story story = parser.parseStory(wholeStory, storyPath);
        List<String> beforeSteps = story.getLifecycle().getBeforeSteps(Scope.SCENARIO);
        assertThat(beforeSteps.isEmpty(), equalTo(true));
        List<String> afterSteps = story.getLifecycle().getAfterSteps(Scope.SCENARIO);
        assertThat(afterSteps.get(0), equalTo("Given a step after each scenario"));
        assertThat(afterSteps.get(1), equalTo("And another after step"));
        Scenario scenario = story.getScenarios().get(0);
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario"));
    }

    @Test
    void shouldParseStoryWithLifecycleAfterUponOutcome() {
        String wholeStory = "Lifecycle: " + NL +
                "After:" + NL + NL +
                "Outcome: ANY " + NL +
                "Given a step after any scenario" + NL + 
                "Outcome: SUCCESS " + NL +
                "Given a step after successful scenario" + NL + 
                "Outcome: FAILURE " + NL +
                "Given a step after failed scenario" + NL + 
                "Scenario:" +NL +
                "Given a scenario";
        Story story = parser.parseStory(wholeStory, storyPath);
        List<String> beforeSteps = story.getLifecycle().getBeforeSteps(Scope.SCENARIO);
        assertThat(beforeSteps.isEmpty(), equalTo(true));
        Lifecycle lifecycle = story.getLifecycle();
        List<String> afterSteps = lifecycle.getAfterSteps(Scope.SCENARIO);
        assertThat(afterSteps.get(0), equalTo("Given a step after any scenario"));
        assertThat(afterSteps.get(1), equalTo("Given a step after successful scenario"));
        assertThat(afterSteps.get(2), equalTo("Given a step after failed scenario"));
        assertThat(lifecycle.getAfterSteps(Outcome.ANY).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.ANY).get(0), equalTo("Given a step after any scenario"));
        assertThat(lifecycle.getAfterSteps(Outcome.SUCCESS).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.SUCCESS).get(0), equalTo("Given a step after successful scenario"));
        assertThat(lifecycle.getAfterSteps(Outcome.FAILURE).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.FAILURE).get(0), equalTo("Given a step after failed scenario"));
        Scenario scenario = story.getScenarios().get(0);
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario"));
    }


    @Test
    void shouldParseStoryWithLifecycleAfterUponOutcomeAndMetaFilter() {
        String wholeStory = "Lifecycle: " + NL +
                "After:" + NL + NL +
                "Outcome: ANY " + NL +
                "MetaFilter: +all" + NL +
                "Given a step after any scenario" + NL + 
                "Outcome: SUCCESS " + NL +
                "MetaFilter: +happy" + NL +
                "Given a step after successful scenario" + NL + 
                "Outcome: FAILURE " + NL +
                "MetaFilter: +sad" + NL +
                "Given a step after failed scenario" + NL + 
                "Scenario:" +NL +
                "Given a scenario";
        Story story = parser.parseStory(wholeStory, storyPath);
        List<String> beforeSteps = story.getLifecycle().getBeforeSteps(Scope.SCENARIO);
        assertThat(beforeSteps.isEmpty(), equalTo(true));
        Lifecycle lifecycle = story.getLifecycle();
        List<String> afterSteps = lifecycle.getAfterSteps(Scope.SCENARIO);
        assertThat(afterSteps.get(0), equalTo("Given a step after any scenario"));
        assertThat(afterSteps.get(1), equalTo("Given a step after successful scenario"));
        assertThat(afterSteps.get(2), equalTo("Given a step after failed scenario"));
        assertThat(new ArrayList<>(lifecycle.getOutcomes()), equalTo(Arrays.asList(Outcome.ANY, Outcome.SUCCESS, Outcome.FAILURE)));
        assertThat(lifecycle.getAfterSteps(Outcome.ANY).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.ANY).get(0), equalTo("Given a step after any scenario"));
        assertThat(lifecycle.getAfterSteps(Outcome.SUCCESS).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.SUCCESS).get(0), equalTo("Given a step after successful scenario"));
        assertThat(lifecycle.getAfterSteps(Outcome.FAILURE).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.FAILURE).get(0), equalTo("Given a step after failed scenario"));
        assertThat(lifecycle.getMetaFilter(Outcome.ANY).asString(), equalTo("+all"));
        Keywords keywords = new Keywords();
        assertThat(lifecycle.getAfterSteps(Outcome.ANY, Meta.createMeta("@all", keywords)).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.ANY, Meta.createMeta("@all", keywords)).get(0), equalTo("Given a step after any scenario"));
        assertThat(lifecycle.getAfterSteps(Outcome.ANY, Meta.createMeta("@none", keywords)).size(), equalTo(0));
        assertThat(lifecycle.getMetaFilter(Outcome.SUCCESS).asString(), equalTo("+happy"));
        assertThat(lifecycle.getAfterSteps(Outcome.SUCCESS, Meta.createMeta("@happy", keywords)).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.SUCCESS, Meta.createMeta("@happy", keywords)).get(0), equalTo("Given a step after successful scenario"));
        assertThat(lifecycle.getAfterSteps(Outcome.SUCCESS, Meta.createMeta("@none", keywords)).size(), equalTo(0));
        assertThat(lifecycle.getMetaFilter(Outcome.FAILURE).asString(), equalTo("+sad"));        
        assertThat(lifecycle.getAfterSteps(Outcome.FAILURE, Meta.createMeta("@sad", keywords)).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.FAILURE, Meta.createMeta("@sad", keywords)).get(0), equalTo("Given a step after failed scenario"));
        assertThat(lifecycle.getAfterSteps(Outcome.FAILURE, Meta.createMeta("@none", keywords)).size(), equalTo(0));
        Scenario scenario = story.getScenarios().get(0);
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario"));
    }

    @Test
    void shouldParseStoryWithLifecycleExamplesOnly() {
        String wholeStory = "Lifecycle: " + NL +
                "Examples:" + NL +
                "|one|two|three|" + NL +
                "|11|12|13|" + NL +
                "|21|22|23|" + NL +
                "Scenario:" +NL +
                "Given a scenario";
        Story story = parser.parseStory(wholeStory, storyPath);
        ExamplesTable table = story.getLifecycle().getExamplesTable();
        assertThat(table.asString(), equalTo(
                "|one|two|three|" + NL +
                        "|11|12|13|" + NL +
                        "|21|22|23|" + NL));
        Scenario scenario = story.getScenarios().get(0);
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given a scenario"));
    }

    @Test
    void shouldParseStoryWithLifecycleAfterUponOutcomeInNonEnglishLocale() {
        String wholeStory = "Lebenszyklus: " + NL +
                "Nach:" + NL + NL +
                "Ergebnis: JEDES " + NL +
                "Gegeben im Lager sind 200 T-Shirts" + NL + 
                "Ergebnis: ERFOLG " + NL +
                "Gegeben im Lager sind 300 T-Shirts" + NL + 
                "Ergebnis: FEHLER " + NL +
                "Gegeben im Lager sind 400 T-Shirts" + NL + 
                "Szenario:" +NL +
                "Wenn ein Kunde 20 T-Shirts bestellt";
        parser = new RegexStoryParser(new LocalizedKeywords(Locale.GERMAN), new LoadFromClasspath(),
                new TableTransformers());
        Story story = parser.parseStory(wholeStory, storyPath);
        List<String> beforeSteps = story.getLifecycle().getBeforeSteps(Scope.SCENARIO);
        assertThat(beforeSteps.isEmpty(), equalTo(true));
        Lifecycle lifecycle = story.getLifecycle();
        List<String> afterSteps = lifecycle.getAfterSteps(Scope.SCENARIO);
        assertThat(afterSteps.get(0), equalTo("Gegeben im Lager sind 200 T-Shirts"));
        assertThat(afterSteps.get(1), equalTo("Gegeben im Lager sind 300 T-Shirts"));
        assertThat(afterSteps.get(2), equalTo("Gegeben im Lager sind 400 T-Shirts"));
        assertThat(lifecycle.getAfterSteps(Outcome.ANY).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.ANY).get(0), equalTo("Gegeben im Lager sind 200 T-Shirts"));
        assertThat(lifecycle.getAfterSteps(Outcome.SUCCESS).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.SUCCESS).get(0), equalTo("Gegeben im Lager sind 300 T-Shirts"));
        assertThat(lifecycle.getAfterSteps(Outcome.FAILURE).size(), equalTo(1));
        assertThat(lifecycle.getAfterSteps(Outcome.FAILURE).get(0), equalTo("Gegeben im Lager sind 400 T-Shirts"));
        Scenario scenario = story.getScenarios().get(0);
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Wenn ein Kunde 20 T-Shirts bestellt"));
    }

    @Test
    void shouldParseStoryWithGivenStoriesAndExamplesCommentedOut() {
        String wholeStory = "Scenario: Show that we can comment out GivenStories and Examples portions of a scenario" +NL +
                "!-- GivenStories: AGivenStoryToBeCommented" + NL +
                "Given a scenario Given" + NL +
                "When I parse it to When" + NL +
                "And I parse it to And" + NL +
                "!-- And ignore me too" + NL +
                "Then I should get steps Then" + NL +
                "!-- Examples:" + NL +
                "|Comment|Me|Out|" + NL +
                "|yes|we|can|" + NL;
        Story story = parser.parseStory(
                wholeStory, storyPath);

        Scenario scenario = story.getScenarios().get(0);
        assertThat(scenario.getTitle(), equalTo("Show that we can comment out GivenStories and Examples portions of a scenario"));
        assertThat(scenario.getGivenStories().getPaths(), equalTo(Arrays.<String>asList()));
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("!-- GivenStories: AGivenStoryToBeCommented"));
        assertThat(steps.get(1), equalTo("Given a scenario Given"));
        assertThat(steps.get(2), equalTo("When I parse it to When"));
        assertThat(steps.get(3), equalTo("And I parse it to And"));
        assertThat(steps.get(4), equalTo("!-- And ignore me too"));
        assertThat(steps.get(5), equalTo("Then I should get steps Then"));
        assertThat(steps.get(6), 
                equalTo("!-- Examples:" + NL +
                        "|Comment|Me|Out|" + NL +
                        "|yes|we|can|"));
        assertThat(scenario.getExamplesTable().asString(), equalTo(EMPTY));
    }
    
    @Test
    void shouldParseStoryWithMultilineSteps() {
        String wholeStory = "Given a scenario" + NL +
                "with this line" + NL +
                "When I parse it" + NL +
                "with another line" + NL + NL +
                "Then I should get steps" + NL +
                "without worrying about lines" + NL +
                "or extra white space between or after steps" + NL + NL;
        Story story = parser.parseStory(wholeStory, storyPath);

        List<String> steps = story.getScenarios().get(0).getSteps();

        assertThat(steps.get(0), equalTo("Given a scenario" + NL +
                "with this line"));
        assertThat(steps.get(1), equalTo("When I parse it" + NL +
                "with another line"));
        assertThat(steps.get(2), equalTo("Then I should get steps" + NL +
                "without worrying about lines" + NL +
                "or extra white space between or after steps"));
    }

    @Test
    void shouldParseStoryWithMultilineScenarioTitle() {
        String wholeStory = "Scenario: A title\n that is spread across\n multiple lines" + NL + NL +
                "Given a step that's pending" + NL +
                "When I run the scenario" + NL +
                "Then I should see this in the output";

        Story story = parser.parseStory(wholeStory, null);

        assertThat(story.getScenarios().get(0).getTitle(), equalTo("A title\n that is spread across\n multiple lines"));
    }

    @Test
    void shouldParseWithMultipleScenarios() {
        String wholeStory = "Scenario: the first scenario " + NL + NL +
                "Given my scenario" + NL + NL +
                "Scenario: the second scenario" + NL + NL +
                "Given my second scenario";
        Story story = parser.parseStory(wholeStory, storyPath);

        assertThat(story.getScenarios().get(0).getTitle(), equalTo("the first scenario"));
        assertThat(story.getScenarios().get(0).getSteps(), equalTo(asList("Given my scenario")));
        assertThat(story.getScenarios().get(1).getTitle(), equalTo("the second scenario"));
        assertThat(story.getScenarios().get(1).getSteps(), equalTo(asList("Given my second scenario")));
    }

    @Test
    void shouldParseStoryWithDescriptionAndNarrative() {
        String wholeStory = "Story: This is free-text description" +NL +
                "Narrative: This bit of text is ignored" + NL +
                "In order to renovate my house" + NL +
                "As a customer" + NL +
                "I want to get a loan" + NL +
                "Scenario:  A first scenario";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        Description description = story.getDescription();
        assertThat(description.asString(), equalTo("Story: This is free-text description"));
        Narrative narrative = story.getNarrative();
        assertThat(narrative.isEmpty(), not(true));
        assertThat(narrative.inOrderTo(), equalTo("renovate my house"));
        assertThat(narrative.asA(), equalTo("customer"));
        assertThat(narrative.iWantTo(), equalTo("get a loan"));
    }

    @Test
    void shouldParseStoryWithAlternativeNarrative() {
        String wholeStory = "Story: This is free-text description" +NL +
                "Narrative: This is an alternative narrative" + NL +
                "As a customer" + NL +
                "I want to get a loan" + NL +
                "So that I can renovate my house" + NL +
                "Scenario:  A first scenario";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        Description description = story.getDescription();
        assertThat(description.asString(), equalTo("Story: This is free-text description"));
        Narrative narrative = story.getNarrative();
        assertThat(narrative.isEmpty(), not(true));
        assertThat(narrative.asA(), equalTo("customer"));
        assertThat(narrative.iWantTo(), equalTo("get a loan"));
        assertThat(narrative.soThat(), equalTo("I can renovate my house"));
    }

    @Test
    void shouldParseStoryWithIncompleteNarrative() {
        String wholeStory = "Story: This is free-text description" +NL +
                "Narrative: This is an incomplete narrative" + NL +
                "In order to renovate my house" + NL +
                "As a customer" + NL +
                "Scenario:  A first scenario";
        Story story = parser.parseStory(
                wholeStory, storyPath);
        Description description = story.getDescription();
        assertThat(description.asString(), equalTo("Story: This is free-text description"));
        Narrative narrative = story.getNarrative();
        assertThat(narrative.isEmpty(), is(true));
    }

    @Test
    void shouldParseStoryWithAllElements() {
        String wholeStory = "This is just a story description" + NL + NL +

                "Narrative: " + NL +
                "In order to see what we're not delivering" + NL + NL +
                "As a developer" + NL +
                "I want to see the narrative for my story when a scenario in that story breaks" + NL +

                "GivenStories: path1,path2" + NL + NL +
                
                "Lifecycle: " + NL +
                "Before: " + NL + NL+
                "Given a setup step" + NL +
                "After: " + NL + NL+
                "Then a teardown step" + NL +

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

        assertThat(story.toString(), containsString("This is just a story description"));
        assertThat(story.getDescription().asString(), equalTo("This is just a story description"));

        assertThat(story.toString(), containsString("Narrative"));
        assertThat(story.getNarrative().inOrderTo(), equalTo("see what we're not delivering"));
        assertThat(story.getNarrative().asA(), equalTo("developer"));
        assertThat(story.getNarrative().iWantTo(), equalTo("see the narrative for my story when a scenario in that story breaks"));

        assertThat(story.getGivenStories().getPaths(), hasItem("path1"));
        assertThat(story.getGivenStories().getPaths(), hasItem("path2"));

        assertThat(story.toString(), containsString("Lifecycle"));
        assertThat(story.getLifecycle().getBeforeSteps(Scope.SCENARIO).size(), equalTo(1));
        assertThat(story.getLifecycle().getBeforeSteps(Scope.SCENARIO), hasItem("Given a setup step"));
        assertThat(story.getLifecycle().getAfterSteps(Scope.SCENARIO).size(), equalTo(1));
        assertThat(story.getLifecycle().getAfterSteps(Scope.SCENARIO), hasItem("Then a teardown step"));

        Meta storyAsMeta = story.asMeta("story_");
        assertThat(storyAsMeta.getProperty("story_path"), equalTo(story.getPath()));
        assertThat(storyAsMeta.getProperty("story_description"), equalTo(story.getDescription().asString()));
        assertThat(storyAsMeta.getProperty("story_narrative"), equalTo(story.getNarrative().toString()));
        
        assertThat(story.toString(), containsString("A pending scenario"));
        Scenario firstScenario = story.getScenarios().get(0);
        assertThat(firstScenario.getTitle(), equalTo("A pending scenario"));
        assertThat(firstScenario.getGivenStories().getPaths().size(), equalTo(0));
        assertThat(firstScenario.getSteps(), equalTo(asList(
                "Given a step that's pending",
                "When I run the scenario",
                "!-- A comment between steps",
                "Then I should see this in the output"
        )));

        Meta scenarioAsMeta = firstScenario.asMeta("scenario_");
        assertThat(scenarioAsMeta.getProperty("scenario_title"), equalTo(firstScenario.getTitle()));
        assertThat(scenarioAsMeta.getProperty("scenario_givenStories"), equalTo(firstScenario.getGivenStories().asString()));
        assertThat(scenarioAsMeta.getProperty("scenario_examplesTable"), equalTo(firstScenario.getExamplesTable().asString()));

        assertThat(story.toString(), containsString("A passing scenario"));
        Scenario secondScenario = story.getScenarios().get(1);
        assertThat(secondScenario.getTitle(), equalTo("A passing scenario"));
        assertThat(secondScenario.getGivenStories().getPaths().size(), equalTo(0));
        assertThat(secondScenario.getSteps(), equalTo(asList(
                "Given I'm not reporting passing stories",
                "When I run the scenario",
                "Then this should not be in the output"
        )));

        assertThat(story.toString(), containsString("A failing scenario"));
        Scenario thirdScenario = story.getScenarios().get(2);
        assertThat(thirdScenario.getTitle(), equalTo("A failing scenario"));
        assertThat(thirdScenario.getGivenStories().getPaths().size(), equalTo(0));
        assertThat(thirdScenario.getSteps(), equalTo(asList(
                "Given a step that fails",
                "When I run the scenario",
                "Then I should see this in the output",
                "And I should see this in the output"
        )));
    }

    @Test
    void shouldParseStoryWithScenarioKeywordInComments() {
        String wholeStory = "Scenario: with Scenario keyword in comments" + NL +
                "!-- 1 This is a comment" + NL +
                "!-- 2 Scenario: this should not be parsed as a separate scenario" + NL +
                "Given 1st step" + NL +
                "!-- 3 One more comment";

        Story story = parser.parseStory(wholeStory, storyPath);
        List<Scenario> scenarios = story.getScenarios();
        assertThat(scenarios.size(), equalTo(1));
        Scenario scenario = scenarios.get(0);
        assertThat(scenario.getTitle(), equalTo("with Scenario keyword in comments"));
        assertThat(scenario.getSteps(), equalTo(asList(
                "!-- 1 This is a comment",
                "!-- 2 Scenario: this should not be parsed as a separate scenario",
                "Given 1st step",
                "!-- 3 One more comment")));
    }

    @Test
    void shouldParseStoryWithoutScenarioKeywordInComments() {
        String wholeStory = "Scenario: with no Scenario keyword in comments" + NL +
                "Given 1st step" + NL +
                "!-- This is a comment" + NL +
                "Scenario: this should be parsed as a separate scenario" + NL +
                "Given 1st step";

        Story story = parser.parseStory(wholeStory, storyPath);
        List<Scenario> scenarios = story.getScenarios();
        assertThat(scenarios.size(), equalTo(2));
        Scenario scenario = scenarios.get(0);
        assertThat(scenario.getTitle(), equalTo("with no Scenario keyword in comments"));
        assertThat(scenario.getSteps(), equalTo(asList(
                "Given 1st step",
                "!-- This is a comment")));
        Scenario scenario2 = scenarios.get(1);
        assertThat(scenario2.getTitle(), equalTo("this should be parsed as a separate scenario"));
        assertThat(scenario2.getSteps(), equalTo(asList(
                "Given 1st step")));
    }

    void shouldParseStoryWithVeryLongStep() {
        String scenario = aScenarioWithAVeryLongGivenStep();
        ensureThatScenarioCanBeParsed(scenario);
    }

    private String aScenarioWithAVeryLongGivenStep() {
        StringBuilder longScenarioBuilder = new StringBuilder()
                .append("Given all these examples:" + NL)
                .append("|one|two|three|" + NL);
        int numberOfLinesInStep = 50;
        for (int i = 0; i < numberOfLinesInStep; i++) {
            longScenarioBuilder.append("|a|sample|line|" + NL);
        }
        longScenarioBuilder.append("When I do something" + NL);
        longScenarioBuilder.append("Then something should happen" + NL);
        return longScenarioBuilder.toString();
    }

    private void ensureThatScenarioCanBeParsed(String scenarioAsText) {
        parser.parseStory(scenarioAsText);
    }

    @Test
    void shouldParseLongStory() {
         String aGivenWhenThen =
                "Given a step" + NL +
                "When I run it" + NL +
                "Then I should seen an output" + NL;

        StringBuilder aScenario = new StringBuilder();

        aScenario.append("Scenario: A long scenario").append(NL);
        int numberOfGivenWhenThensPerScenario = 50;
        for (int i = 0; i < numberOfGivenWhenThensPerScenario; i++) {
            aScenario.append(aGivenWhenThen);
        }

        int numberOfScenarios = 100;
        StringBuilder wholeStory = new StringBuilder();
        wholeStory.append("Story: A very long story").append(NL);
        for (int i = 0; i < numberOfScenarios; i++) {
            wholeStory.append(aScenario).append(NL);
        }

        Story story = parser.parseStory(wholeStory.toString(), null);
        assertThat(story.getScenarios().size(), equalTo(numberOfScenarios));
        for (Scenario scenario : story.getScenarios()) {
            assertThat(scenario.getSteps().size(), equalTo(numberOfGivenWhenThensPerScenario * 3));
        }
    }

    @Test
    void shouldParseStoryWithScenarioContainingExamplesTable() {
        String wholeStory = "Scenario: A scenario with examples table" + NL + NL +
                "Given a step with a <one>" + NL +
                "When I run the scenario of name <two>" + NL +
                "Then I should see <three> in the output" + NL +

                "Examples:" + NL +
                "|one|two|three|" + NL +
                "|11|12|13|" + NL +
                "|21|22|23|" + NL;

        Story story = parser.parseStory(wholeStory, storyPath);

        Scenario scenario = story.getScenarios().get(0);
        assertThat(scenario.getTitle(), equalTo("A scenario with examples table"));
        assertThat(scenario.getGivenStories().getPaths().size(), equalTo(0));
        assertThat(scenario.getSteps(), equalTo(asList(
                "Given a step with a <one>",
                "When I run the scenario of name <two>",
                "Then I should see <three> in the output"
        )));
        ExamplesTable table = scenario.getExamplesTable();
        assertThat(table.asString(), equalTo(
                "|one|two|three|" + NL +
                        "|11|12|13|" + NL +
                        "|21|22|23|" + NL));
        assertThat(table.getRowCount(), equalTo(2));
        assertThat(table.getRow(0), not(nullValue()));
        assertThat(table.getRow(0).get("one"), equalTo("11"));
        assertThat(table.getRow(0).get("two"), equalTo("12"));
        assertThat(table.getRow(0).get("three"), equalTo("13"));
        assertThat(table.getRow(1), not(nullValue()));
        assertThat(table.getRow(1).get("one"), equalTo("21"));
        assertThat(table.getRow(1).get("two"), equalTo("22"));
        assertThat(table.getRow(1).get("three"), equalTo("23"));
    }

    @Test
    void shouldParseStoryWithScenarioContainingGivenStories() {
        // given stories as CSV with no spaces or newlines
        parseStoryWithGivenStories(
                "GivenStories: path/to/one,path/to/two" + NL + NL +
                "Given a step");
        // given stories as CSV with spaces and newlines
        parseStoryWithGivenStories(
                "GivenStories: path/to/one , " +NL +" path/to/two" + NL + NL +
                "Given a step");
    }

    @Test
    void shouldParseStoryWithScenarioContainingParametrisedGivenStories() {
        String wholeStory = 
            "GivenStories: path/to/one#{0}, path/to/two#{1}, path/to/three#{2}, path/to/four#{a}, path/to/five" + NL + NL +
            "Given a step" + NL+
            "Examples:" + NL +
            "|one|two|" + NL +  
            "|11|12|" + NL +
            "|21|22|";
        Story story = parser.parseStory(wholeStory, storyPath);

        Scenario scenario = story.getScenarios().get(0);
        GivenStories givenStories = scenario.getGivenStories();
        assertThat(givenStories.asString(), equalTo("path/to/one#{0}, path/to/two#{1}, path/to/three#{2}, path/to/four#{a}, path/to/five"));
        assertThat(givenStories.toString(), containsString(givenStories.asString()));
        assertThat(givenStories.getPaths(), equalTo(asList(
                "path/to/one#{0}", // matches first parameters row
                "path/to/two#{1}", // matches second parameters row
                "path/to/three#{2}", // does not match any parameters row
                "path/to/four#{a}", // does not use valid anchor (an int)
                "path/to/five"))); // does not require parameters
        assertThat(givenStories.requireParameters(), equalTo(true));
        GivenStory givenStory1 = givenStories.getStories().get(0);
        assertThat(givenStory1.hasAnchor(), equalTo(true));
        assertThat(givenStory1.getAnchor(), equalTo("0"));
        assertThat(givenStory1.getPath(), equalTo("path/to/one"));
        assertThat(givenStory1.getParameters().get("one"), equalTo("11"));
        assertThat(givenStory1.getParameters().get("two"), equalTo("12"));
        GivenStory givenStory2 = givenStories.getStories().get(1);
        assertThat(givenStory2.hasAnchor(), equalTo(true));
        assertThat(givenStory2.getAnchor(), equalTo("1"));
        assertThat(givenStory2.getPath(), equalTo("path/to/two"));
        assertThat(givenStory2.getParameters().get("one"), equalTo("21"));
        assertThat(givenStory2.getParameters().get("two"), equalTo("22"));
        GivenStory givenStory3 = givenStories.getStories().get(2);
        assertThat(givenStory3.hasAnchor(), equalTo(true));
        assertThat(givenStory3.getAnchor(), equalTo("2"));
        assertThat(givenStory3.getPath(), equalTo("path/to/three"));
        assertThat(givenStory3.getParameters().size(), equalTo(0));
        GivenStory givenStory4 = givenStories.getStories().get(3);
        assertThat(givenStory4.hasAnchor(), equalTo(true));
        assertThat(givenStory4.getAnchor(), equalTo("a"));
        assertThat(givenStory4.getPath(), equalTo("path/to/four"));
        assertThat(givenStory4.getParameters().size(), equalTo(0));
        GivenStory givenStory5 = givenStories.getStories().get(4);
        assertThat(givenStory5.hasAnchor(), equalTo(false));
        assertThat(givenStory5.getAnchor(), equalTo(EMPTY));
        assertThat(givenStory5.getPath(), equalTo("path/to/five"));
        assertThat(givenStory5.getParameters().size(), equalTo(0));
    }

    @Test
    void shouldParseStoryWithScenarioContainingExamplesTableAndEndingWithComments() {
        String wholeStory = "Scenario: A scenario with examples table and comment" + NL + NL +
                "Given a step with a <one>" + NL +
                "Examples:" + NL +
                "|one|two|three|" + NL +
                "|11|12|13|" + NL +
                "|21|22|23|" + NL + NL + NL +
                "!-- Some remark" + NL + NL;

        Story story = parser.parseStory(wholeStory, storyPath);

        Scenario scenario = story.getScenarios().get(0);
        assertThat(scenario.getTitle(), equalTo("A scenario with examples table and comment"));
        assertThat(scenario.getGivenStories().getPaths().size(), equalTo(0));
        assertThat(scenario.getSteps(), equalTo(asList(
                "Given a step with a <one>"
        )));
        ExamplesTable table = scenario.getExamplesTable();
        assertThat(table.asString(), equalTo(
                "|one|two|three|" + NL +
                        "|11|12|13|" + NL +
                        "|21|22|23|" + NL));
        assertThat(table.getRowCount(), equalTo(2));
        assertThat(table.getRow(0), not(nullValue()));
        assertThat(table.getRow(0).get("one"), equalTo("11"));
        assertThat(table.getRow(0).get("two"), equalTo("12"));
        assertThat(table.getRow(0).get("three"), equalTo("13"));
        assertThat(table.getRow(1), not(nullValue()));
        assertThat(table.getRow(1).get("one"), equalTo("21"));
        assertThat(table.getRow(1).get("two"), equalTo("22"));
        assertThat(table.getRow(1).get("three"), equalTo("23"));
    }

    private void parseStoryWithGivenStories(String wholeStory) {
        Story story = parser.parseStory(wholeStory, storyPath);

        Scenario scenario = story.getScenarios().get(0);
        assertThat(scenario.getGivenStories().getPaths(), equalTo(asList(
                "path/to/one",
                "path/to/two")));
        assertThat(scenario.getSteps(), equalTo(asList(
                "Given a step"
        )));
    }

    @Test
    void shouldParseStoryWithoutAPath() {
        String wholeStory = "Given a step" + NL +
                "When I run it" + NL +
                "Then I should an output";

        Story story = parser.parseStory(wholeStory);

        assertThat(story.getPath(), equalTo(EMPTY));
        Scenario scenario = story.getScenarios().get(0);
        assertThat(scenario.getSteps(), equalTo(asList(
                "Given a step",
                "When I run it",
                "Then I should an output"
        )));
    }
    
    @Test
    void shouldParseStoryWithVeryLongTitle() {
        ensureThatScenarioCanBeParsed(aScenarioWithAVeryLongTitle(2000));
    }

    @Test
    void shouldParseStoryWithSingleEmptyScenario() {
        String wholeStory = "Scenario: empty";

        Story story = parser.parseStory(wholeStory);

        List<Scenario> scenarios = story.getScenarios();
        assertThat(scenarios.size(), equalTo(1));

        Scenario scenario = scenarios.get(0);
        assertThat(scenario.getTitle(), equalTo("empty"));
        assertThat(scenario.getSteps(), emptyIterable());
    }

    @Test
    void shouldParseStoryHavingEmptyScenarios() {
        String wholeStory =
                "Scenario: scenario with step" + NL +
                "Given step" + NL + NL +
                "Scenario: empty" +NL + NL +
                "Scenario: one more empty";

        Story story = parser.parseStory(wholeStory);

        List<Scenario> scenarios = story.getScenarios();
        assertThat(scenarios.size(), equalTo(3));

        Scenario scenario1 = scenarios.get(0);
        assertThat(scenario1.getTitle(), equalTo("scenario with step"));
        assertThat(scenario1.getSteps(), equalTo(asList("Given step")));

        Scenario scenario2 = scenarios.get(1);
        assertThat(scenario2.getTitle(), equalTo("empty"));
        assertThat(scenario2.getSteps(), emptyIterable());

        Scenario scenario3 = scenarios.get(2);
        assertThat(scenario3.getTitle(), equalTo("one more empty"));
        assertThat(scenario3.getSteps(), emptyIterable());
    }

    @Test
    void shouldParseStoryWithSingleEmptyScenarioHavingMeta() {
        String wholeStory =
                "Scenario: empty" + NL + NL +
                "Meta: @theme parsing";

        Story story = parser.parseStory(wholeStory);

        List<Scenario> scenarios = story.getScenarios();
        assertThat(scenarios.size(), equalTo(1));

        Scenario scenario = scenarios.get(0);
        assertThat(scenario.getTitle(), equalTo("empty"));
        assertThat(scenario.getSteps(), emptyIterable());
        assertThat(scenario.getMeta().getPropertyNames(), equalTo(singleton("theme")));
        assertThat(scenario.getMeta().getProperty("theme"), equalTo("parsing"));
    }

    @Test
    void shouldParseStoryWithUntrimmedSteps() {
        String wholeStory = 
                "Scenario: A scenario" + NL +
                "Given step " + NL + NL + NL +
                "When step " + NL + NL +
                "Then step";
        Story story = parser.parseStory(wholeStory, storyPath);
        assertThat(story.getPath(), equalTo(storyPath));
        List<Scenario> scenarios = story.getScenarios();
        assertThat(scenarios.size(), equalTo(1));
        Scenario scenario = scenarios.get(0);
        assertThat(scenario.getTitle(), equalTo("A scenario"));
        List<String> steps = scenario.getSteps();
        assertThat(steps.get(0), equalTo("Given step"));
        assertThat(steps.get(1), equalTo("When step"));
        assertThat(steps.get(2), equalTo("Then step"));
        List<String> trimmedSteps = scenario.getSteps(true);
        assertThat(trimmedSteps.get(0), equalTo("Given step"));
        assertThat(trimmedSteps.get(1), equalTo("When step"));
        assertThat(trimmedSteps.get(2), equalTo("Then step"));
        List<String> untrimmedSteps = scenario.getSteps(false);
        assertThat(untrimmedSteps.get(0), equalTo("Given step " + NL + NL));
        assertThat(untrimmedSteps.get(1), equalTo("When step " + NL));
        assertThat(untrimmedSteps.get(2), equalTo("Then step"));
    }

    private String aScenarioWithAVeryLongTitle(int numberOfLines) {
        StringBuilder builder = new StringBuilder();        
        builder.append("Scenario: First line of long title." + NL)
               .append("After that follows a long textual description. " + NL);
        
        for (int i = 0; i < numberOfLines; i++) {
            builder.append("A line from the long description with about 60 characters." + NL);
        }
        builder.append("Given the first step that marks end of title" + NL);
        return builder.toString();
    }

    @Test
    void shouldParseStoryWithVeryLongTables() {
        ensureThatScenarioCanBeParsed(aScenarioWithVeryLongTables(2000));
    }

    private String aScenarioWithVeryLongTables(int numberOfLines) {
        StringBuilder builder = new StringBuilder();        
        builder.append("Scenario: A scenario with long tables" + NL)
               .append("GivenStories: path1,path2,path3" + NL);
        builder.append("Given a step with a long tabular argument: " + NL)  
               .append(aTableWith(numberOfLines));        
        builder.append("Examples:" + NL)       
               .append(aTableWith(numberOfLines));
        return builder.toString();
    }

    private String aTableWith(int numberOfLines) {
        StringBuilder builder = new StringBuilder();        
        builder.append("|h0|h1|h2|h3|h4|h5|h6|h7|h8|h9|" + NL);
        for (int i = 0; i < numberOfLines; i++) {
            builder.append("|c"+i + "0|c"+i + "1|c"+i + "2|c"+i + "3|c"+i + "4|c"+i + "5|c"+i + "6|c"+i + "7|c"+i + "8|c"+i + "9|" + NL);
        }
        return builder.toString();
    }

}
