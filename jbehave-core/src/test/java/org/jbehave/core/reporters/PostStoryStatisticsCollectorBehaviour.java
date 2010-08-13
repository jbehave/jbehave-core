package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.junit.Test;

public class PostStoryStatisticsCollectorBehaviour {

    @Test
    public void shouldCollectStoryStatistics() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new PostStoryStatisticsCollector(new PrintStream(out));

        // When
        narrateAnInterestingStory(reporter);
        
        // Then
        assertThat(out.toString(), containsString("examples=2"));
        assertThat(out.toString(), containsString("givenStories=1"));
        assertThat(out.toString(), containsString("steps=7"));
        assertThat(out.toString(), containsString("stepsFailed=1"));
        assertThat(out.toString(), containsString("stepsPending=1"));
        assertThat(out.toString(), containsString("stepsIgnorable=1"));
        assertThat(out.toString(), containsString("stepsNotPerformed=1"));
        assertThat(out.toString(), containsString("stepsSuccessful=3"));
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

    private void narrateAnInterestingStory(StoryReporter reporter) {
        Story story = new Story("/path/to/story",
                new Description("An interesting story"), new Narrative("renovate my house", "customer", "get a loan"), new ArrayList<Scenario>());
        boolean givenStory = false;
        reporter.beforeStory(story, givenStory);
        String title = "I ask for a loan";
        reporter.beforeScenario(title);
        reporter.givenStories(asList("/given/story1,/given/story2"));
        reporter.successful("Given I have a balance of $50");
        reporter.ignorable("!-- A comment");
        reporter.successful("When I request $20");
        reporter.successful("When I ask Liz for a loan of $100");
        reporter.pending("Then I should have a balance of $30");
        reporter.notPerformed("Then I should have $20");
        OutcomesTable outcomesTable = new OutcomesTable();
        outcomesTable.addOutcome("I don't return all", 100.0, equalTo(50.));
        try {
        	outcomesTable.verify();
        } catch ( OutcomesFailed e ){
        	reporter.failedOutcomes("Then I don't return loan", e.outcomesTable());
        }
        ExamplesTable table = new ExamplesTable("|money|to|\n|$30|Mauro|\n|$50|Paul|\n");
        reporter.beforeExamples(asList("Given money <money>", "Then I give it to <to>"), table);
        reporter.example(table.getRow(0));
        reporter.example(table.getRow(1));
        reporter.afterExamples();
        reporter.afterScenario();
        reporter.afterStory(givenStory);
    }

}
