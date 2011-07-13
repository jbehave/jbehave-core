package org.jbehave.core.reporters;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import org.hamcrest.Matchers;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class NullStoryReporterBehaviour {

    private final String NL = System.getProperty("line.separator");

    @Test
    public void shouldOnlyReportOverriddenMethods() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(out);
        StoryReporter reporter = new NullStoryReporter(){

            @Override
            public void beforeStory(Story story, boolean givenStory) {
                printStream.println("beforeStory");
            }

            @Override
            public void afterStory(boolean givenStory) {
                printStream.println("afterStory");
            }

        };
        narrateAnInterestingStory(reporter);
        assertThat(out.toString(), Matchers.equalTo("beforeStory" + NL + "afterStory" + NL));
    }


    private void narrateAnInterestingStory(StoryReporter reporter) {
        Story story = new Story("/path/to/story", new Description("An interesting story"), new Narrative(
                "renovate my house", "customer", "get a loan"), new ArrayList<Scenario>());
        reporter.dryRun();
        reporter.beforeStory(story, false);

        // successful scenario
        reporter.beforeScenario("A successful scenario");
        reporter.successful("Given I have a balance of $50");
        reporter.ignorable("!-- A comment");
        reporter.successful("When I request $20");
        reporter.successful("When I ask Liz for a loan of $100");
        reporter.afterScenario();

        // failing scenario
        reporter.beforeScenario("A failing scenario");
        OutcomesTable outcomesTable = new OutcomesTable();
        outcomesTable.addOutcome("I don't return all", 100.0, equalTo(50.));
        try {
            outcomesTable.verify();
        } catch (UUIDExceptionWrapper e) {
            reporter.failedOutcomes("Then I don't return loan", ((OutcomesFailed) e.getCause()).outcomesTable());
        }
        reporter.pending("Then I should have a balance of $30");
        reporter.notPerformed("Then I should have $20");
        ExamplesTable table = new ExamplesTable("|money|to|\n|$30|Mauro|\n|$50|Paul|\n");
        reporter.beforeExamples(asList("Given money <money>", "Then I give it to <to>"), table);
        reporter.example(table.getRow(0));
        reporter.example(table.getRow(1));
        reporter.afterExamples();
        reporter.afterScenario();
        reporter.afterStory(false);
    }

}
