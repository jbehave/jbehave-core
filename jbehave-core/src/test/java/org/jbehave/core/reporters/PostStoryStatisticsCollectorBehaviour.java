package org.jbehave.core.reporters;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class PostStoryStatisticsCollectorBehaviour {
    private OutputStream out;
    private PrintStream printStream;

    private PostStoryStatisticsCollector reporter;

    @Before
    public void setUp() throws Exception {
        out = new ByteArrayOutputStream();
        printStream = new PrintStream(out);
        reporter = new PostStoryStatisticsCollector(printStream);
    }

    @Test
    public void shouldCollectStoryStatistics() {
        // When
        narrateAnInterestingStory();

        // Then
        String statistics = out.toString();
        assertThat(statistics, containsString("scenarios=3"));
        assertThat(statistics, containsString("scenariosSuccessful=2"));
        assertThat(statistics, containsString("scenariosPending=1"));
        assertThat(statistics, containsString("scenariosFailed=1"));

        assertThat(statistics, containsString("givenStories=2"));
        assertThat(statistics, containsString("givenStoryScenarios=3"));
        assertThat(statistics, containsString("givenStoryScenariosSuccessful=3"));
        assertThat(statistics, containsString("givenStoryScenariosPending=1"));
        assertThat(statistics, containsString("givenStoryScenariosFailed=0"));

        assertThat(statistics, containsString("steps=9"));
        assertThat(statistics, containsString("stepsFailed=1"));
        assertThat(statistics, containsString("stepsPending=1"));
        assertThat(statistics, containsString("stepsIgnorable=1"));
        assertThat(statistics, containsString("stepsNotPerformed=2"));
        assertThat(statistics, containsString("stepsSuccessful=4"));

        assertThat(statistics, containsString("storyPending=1"));

        assertThat(statistics, containsString("examples=2"));

        assertThat(reporter.toString(), containsString(printStream.toString()));
    }

    @Test
    public void shouldCollectStoryStatisticsWhenStoryNotAllowedByFilter() {
        // When
        narrateAnInterestingStoryNotAllowedByFilter(true);

        // Then
        String statistics = out.toString();
        assertThat(statistics, containsString("notAllowed=1"));
    }

    @Test
    public void shouldCollectStoryStatisticsWhenScenariosNotAllowedByFilter() {
        // When
        narrateAnInterestingStoryNotAllowedByFilter(false);
        
        // Then
        String statistics = out.toString();
        assertThat(statistics, containsString("notAllowed=0"));
        assertThat(statistics, containsString("scenariosNotAllowed=1"));
    }

    @Test
    public void shouldNotCountFailedScenariosIfExceptionsAreNull() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new PostStoryStatisticsCollector(new PrintStream(out));

        // When
        reporter.failed("a failed step", null);
        reporter.failedOutcomes("some failed outcomes", null);

        // Then
        assertThat(out.toString(), not(containsString("scenariosFailed")));
    }

    private void narrateAnInterestingStory() {
        Story story = new Story("/path/to/story", new Description("An interesting story"), new Narrative(
                "renovate my house", "customer", "get a loan"), new ArrayList<Scenario>());
        reporter.dryRun();
        // begin story
        reporter.beforeStory(story, false);

        // 1st scenario
        reporter.beforeScenario("I ask for a loan");
        reporter.scenarioMeta(Meta.EMPTY);
        reporter.givenStories(asList("path/to/story1", "path/to/story2"));

        // 1st given story
        reporter.beforeStory(story, true);
        reporter.beforeScenario("a scenario without steps");
        reporter.afterScenario();
        reporter.afterStory(true);

        // 2nd given story
        reporter.beforeStory(story, true);
        reporter.beforeScenario("the bank has $300 to loan");
        reporter.givenStories(asList("path/to/nested/story1"));

        reporter.beforeStory(story, true);
        reporter.beforeScenario("initialise static");
        reporter.successful("the bank has customers");
        reporter.afterScenario();
        reporter.afterStory(true);

        reporter.afterScenario();
        reporter.afterStory(true);

        reporter.successful("Given I have a balance of $50");
        reporter.ignorable("!-- A comment");
        reporter.successful("When I request $20");
        reporter.successful("When I ask Liz for a loan of $100");
        reporter.afterScenario();

        // 2nd scenario
        reporter.beforeScenario("A failing scenario");
        OutcomesTable outcomesTable = new OutcomesTable();
        outcomesTable.addOutcome("I don't return all", 100.0, equalTo(50.));
        try {
            outcomesTable.verify();
        } catch (UUIDExceptionWrapper e) {
            reporter.failedOutcomes("Then I don't return loan", ((OutcomesFailed)e.getCause()).outcomesTable());
        }
        reporter.notPerformed("Then I should have $20");
        ExamplesTable table = new ExamplesTable("|money|to|\n|$30|Mauro|\n|$50|Paul|\n");
        reporter.beforeExamples(asList("Given money <money>", "Then I give it to <to>"), table);
        reporter.example(table.getRow(0));
        reporter.example(table.getRow(1));
        reporter.afterExamples();
        reporter.afterScenario();

        // 3rd scenario
        reporter.beforeScenario("A pending scenario");
        reporter.pending("When I have some money");
        reporter.notPerformed("Then I should have $20");
        reporter.afterScenario();        
        
        // end story
        reporter.afterStory(false);
    }

    private void narrateAnInterestingStoryNotAllowedByFilter(boolean storyNotAllowed) {
        Properties meta = new Properties();
        meta.setProperty("theme", "testing");
        meta.setProperty("author", "Mauro");
        Story story = new Story("/path/to/story", new Description("An interesting story"), new Meta(meta),
                new Narrative("renovate my house", "customer", "get a loan"), Arrays.asList(new Scenario("A scenario",
                        new Meta(meta), GivenStories.EMPTY, ExamplesTable.EMPTY, new ArrayList<String>())));
        if (storyNotAllowed) {
            reporter.storyNotAllowed(story, "-theme testing");
        } else {
            reporter.beforeStory(story, false);
            reporter.scenarioNotAllowed(story.getScenarios().get(0), "-theme testing");
            reporter.afterStory(false);
        }
    }
}
