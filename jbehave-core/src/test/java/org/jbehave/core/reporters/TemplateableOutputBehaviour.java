package org.jbehave.core.reporters;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.jbehave.core.failures.RestartingScenarioFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.junit.Test;
import org.xml.sax.SAXException;

import static java.util.Arrays.asList;

import static org.hamcrest.Matchers.equalTo;

import static org.junit.Assert.assertEquals;

public class TemplateableOutputBehaviour {

    @Test
    public void shouldReportEventsToHtmlOutput() throws IOException {
        // Given
        File file = new File("target/story.html");
        StoryReporter reporter = new HtmlTemplateOutput(file, new LocalizedKeywords());

        // When
        narrateAnInterestingStory(reporter, true);

        // Then
        String expected = IOUtils.toString(new FileReader(new File("src/test/resources/story.html")));
        String out = IOUtils.toString(new FileReader(file));
        assertThatOutputIs(out, expected);
    }

    @Test
    public void shouldReportEventsToXmlOutput() throws IOException, SAXException {
        // Given
        File file = new File("target/story.xml");
        StoryReporter reporter = new XmlTemplateOuput(file, new LocalizedKeywords());

        // When
        narrateAnInterestingStory(reporter, true);

        // Then
        String expected = IOUtils.toString(new FileReader(new File("src/test/resources/story.xml")));
        String out = IOUtils.toString(new FileReader(file));

        // will throw SAXException if the xml file is not well-formed
        XMLUnit.buildTestDocument(out);
        assertThatOutputIs(out, expected);
    }

    public static void narrateAnInterestingStory(StoryReporter reporter, boolean withFailure) {
        Properties meta = new Properties();
        meta.setProperty("theme", "testing");
        meta.setProperty("author", "Mauro");
        Story story = new Story("/path/to/story", new Description("An interesting story"), new Meta(meta),
                new Narrative("renovate my house", "customer", "get a loan"), new ArrayList<Scenario>());
        boolean givenStory = false;
        reporter.dryRun();
        reporter.beforeStory(story, givenStory);
        reporter.narrative(story.getNarrative());
        reporter.beforeScenario("I ask for a loan");
        reporter.givenStories(asList("/given/story1", "/given/story2"));
        reporter.successful("Given I have a balance of $50");
        reporter.ignorable("!-- A comment");
        reporter.successful("When I request $20");
        reporter.successful("When I ask Liz for a loan of $100");
        reporter.restarted("Then I should... - try again", new RestartingScenarioFailure("hi"));
        if (withFailure) {
            reporter.failed("Then I should have a balance of $30", new Exception("Expected <30> got <25>"));
        } else {
            reporter.pending("Then I should have a balance of $30");
        }
        reporter.notPerformed("Then I should have $20");
        OutcomesTable outcomesTable = new OutcomesTable();
        outcomesTable.addOutcome("I don't return all", 100.0, equalTo(50.));
        try {
            outcomesTable.verify();
        } catch (UUIDExceptionWrapper e) {
            reporter.failedOutcomes("Then I don't return loan", ((OutcomesFailed) e.getCause()).outcomesTable());
        }
        reporter.afterScenario();
        reporter.beforeScenario("Parametrised Scenario");
        ExamplesTable table = new ExamplesTable("|money|to|\n|$30|Mauro|\n|$50|Paul|\n");
        reporter.beforeExamples(asList("Given money <money>", "Then I give it to <to>"), table);
        reporter.example(table.getRow(0));
        reporter.successful("Given money $30");
        reporter.successful("Then I give it to Mauro");
        reporter.example(table.getRow(1));
        reporter.successful("Given money $50");
        reporter.successful("Then I give it to Paul");
        if (withFailure) {
            reporter.failed("Then I should have a balance of $30", new Exception("Expected <30> got <25>"));
        } else {
            reporter.pending("Then I should have a balance of $30");
        }
        reporter.afterExamples();
        reporter.afterScenario();
        reporter.storyCancelled(story, new StoryDuration(2, 1));
        String method1="@When(\"something \\\"$param\\\"\")\n"
                + "@Pending\n"
                + "public void whenSomething() {\n"
                + "  // PENDING\n"
                + "}\n";
        String method2="@Then(\"something is <param1>\")\n"
                + "@Pending\n"
                + "public void thenSomethingIsParam1() {\n"
                + "  // PENDING\n"
                + "}\n";
        reporter.pendingMethods(asList(method1, method2));
        reporter.afterStory(givenStory);
    }

    private void assertThatOutputIs(String out, String expected) {
        assertEquals(dos2unix(expected), dos2unix(out));
    }

    private String dos2unix(String string) {
        return string.replace("\r\n", "\n");
    }

}
