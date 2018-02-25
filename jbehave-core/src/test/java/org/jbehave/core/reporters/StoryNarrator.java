package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.hamcrest.core.IsEqual;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.failures.RestartingScenarioFailure;
import org.jbehave.core.failures.RestartingStoryFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.*;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.steps.StepCreator;

class StoryNarrator {

    static void narrateAnInterestingStory(StoryReporter reporter,
            boolean withFailure) {
        Properties meta = new Properties();
        meta.setProperty("theme", "testing");
        meta.setProperty("author", "Mauro");
        Lifecycle.Steps beforeScenarioSteps = new Lifecycle.Steps(Scope.SCENARIO, asList("Given a scenario step"));
        Lifecycle.Steps beforeStorySteps = new Lifecycle.Steps(Scope.STORY, asList("Given a story step"));
        Lifecycle.Steps afterScenarioSteps = new Lifecycle.Steps(Scope.SCENARIO, asList("Given a scenario step"));
        Lifecycle.Steps afterStorySteps = new Lifecycle.Steps(Scope.STORY, asList("Given a story step"));
        Lifecycle lifecycle = new Lifecycle(asList(beforeScenarioSteps, beforeStorySteps), asList(afterScenarioSteps, afterStorySteps));
        Story story = new Story("/path/to/story", new Description("An interesting story & special chars"), new Meta(meta),
                new Narrative("renovate my house", "customer", "get a loan"), GivenStories.EMPTY, lifecycle, new ArrayList<Scenario>());
        boolean givenStory = false;
        reporter.beforeStory(story, givenStory);
        reporter.dryRun();
        reporter.narrative(story.getNarrative());
        reporter.beforeScenario("I ask for a loan");
        reporter.beforeGivenStories();
        reporter.givenStories(asList("/given/story1", "/given/story2"));
        reporter.afterGivenStories();
        reporter.successful("Given I have a balance of $50");
        reporter.ignorable("!-- Then ignore me");
        reporter.comment("!-- A comment");
        reporter.successful("When I request $20");
        reporter.successful("When I ask Liz for a loan of $100");
        reporter.successful("When I ask Liz for a loan of $"+StepCreator.PARAMETER_VALUE_START+"99"+StepCreator.PARAMETER_VALUE_END);
        reporter.successful("When I write special chars <>&\"");
        reporter.successful("When I write special chars in parameter "+StepCreator.PARAMETER_VALUE_START+"<>&\""+StepCreator.PARAMETER_VALUE_END);
        reporter.successful("When I write two parameters "
                + StepCreator.PARAMETER_VALUE_START+",,,"+StepCreator.PARAMETER_VALUE_END
                + " and "
                + StepCreator.PARAMETER_VALUE_START+"&&&"+StepCreator.PARAMETER_VALUE_END);
        reporter.restarted("Then I should... - try again", new RestartingScenarioFailure("hi"));
        reporter.restartedStory(story, new RestartingStoryFailure("Restarted Story"));
        reporter.storyCancelled(story, new StoryDuration(1).setDurationInSecs(2));
        if (withFailure) {
            reporter.failed("Then I should have a balance of $30", new Exception("Expected <30> got <25>"));
        } else {
            reporter.pending("Then I should have a balance of $30");
        }
        reporter.notPerformed("Then I should have $20");
        OutcomesTable outcomesTable = new OutcomesTable(new LocalizedKeywords(), "dd/MM/yyyy");
        outcomesTable.addOutcome("I don't return all", 100.0, equalTo(50.));
        Date actualDate = dateFor("01/01/2011");
        Date expectedDate = dateFor("02/01/2011");
        outcomesTable.addOutcome("A wrong date", actualDate, new IsDateEqual(expectedDate, outcomesTable.getDateFormat()));
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

    public static class IsDateEqual extends IsEqual<Date> {

        private Date date;
        private String dateFormat;

        public IsDateEqual(Date equalArg, String dateFormat) {
            super(equalArg);
            this.date = equalArg;
            this.dateFormat = dateFormat;
        }

        @Override
        public void describeTo(org.hamcrest.Description description) {
            description.appendValue(new SimpleDateFormat(dateFormat).format(date));
        }
        
    }
    static Date dateFor(String date) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: doesn't quite match the way stories
    // look when they are excluded
    static void narrateAnInterestingStoryNotAllowedByFilter(StoryReporter reporter, boolean storyNotAllowed) {
        Properties meta = new Properties();
        meta.setProperty("theme", "testing");
        meta.setProperty("author", "Mauro");
        Story story = new Story("/path/to/story",
                new Description("An interesting story"), new Meta(meta), new Narrative("renovate my house", "customer", "get a loan"),
                Arrays.asList(new Scenario("A scenario", new Meta(meta), GivenStories.EMPTY, ExamplesTable.EMPTY, new ArrayList<String>())));
        reporter.beforeStory(story, false);
        if (storyNotAllowed) {
            reporter.storyNotAllowed(story, "-theme testing");
        } else  {
            reporter.beforeScenario(story.getScenarios().get(0).getTitle());
            reporter.scenarioNotAllowed(story.getScenarios().get(0), "-theme testing");
            reporter.afterScenario();
        }
        reporter.afterStory(false);
    }

}
