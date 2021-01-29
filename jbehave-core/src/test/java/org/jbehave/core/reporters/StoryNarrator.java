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
import org.jbehave.core.model.Lifecycle.Steps;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator;

class StoryNarrator {

    static void narrateAnInterestingStory(StoryReporter reporter, boolean withFailure) {
        Properties meta = new Properties();
        meta.setProperty("theme", "testing");
        meta.setProperty("author", "Mauro");
        String beforeStoryStep = "Given a before story step";
        String afterStoryStep = "Given an after story step";
        String scenarioStep = "Given a scenario step";
        Steps beforeScenarioSteps = new Steps(Scope.SCENARIO, asList(scenarioStep));
        Steps beforeStorySteps = new Steps(Scope.STORY, asList(beforeStoryStep));
        Steps afterScenarioSteps = new Steps(Scope.SCENARIO, asList(scenarioStep));
        Steps afterStorySteps = new Steps(Scope.STORY, asList(afterStoryStep));
        Lifecycle lifecycle = new Lifecycle(asList(beforeScenarioSteps, beforeStorySteps), asList(afterScenarioSteps, afterStorySteps));
        Story story = new Story("/path/to/story", new Description("An interesting story & special chars"), new Meta(meta),
                new Narrative("renovate my house", "customer", "get a loan"), GivenStories.EMPTY, lifecycle, new ArrayList<Scenario>());
        boolean givenStory = false;
        reporter.dryRun();
        reporter.beforeStory(story, givenStory);
        reporter.narrative(story.getNarrative());
        reporter.lifecyle(lifecycle);

        reporter.beforeStorySteps(Stage.BEFORE);
        reporter.beforeStep(beforeStoryStep);
        reporter.successful(beforeStoryStep);
        reporter.afterStorySteps(Stage.BEFORE);

        reporter.beforeScenarios();
        reporter.beforeScenario(new Scenario("I ask for a loan", Meta.EMPTY));
        reportScenarioStep(reporter, scenarioStep, Stage.BEFORE);
        reporter.beforeGivenStories();
        reporter.givenStories(asList("/given/story1", "/given/story2"));
        reporter.afterGivenStories();
        reporter.beforeScenarioSteps(null);
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
            reporter.failed("Then I should have a balance of $30", new UUIDExceptionWrapper(new Exception("Expected <30> got <25>")));
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
        reporter.afterScenarioSteps(null);
        reportScenarioStep(reporter, scenarioStep, Stage.AFTER);
        reporter.afterScenario();
        reporter.beforeScenario(new Scenario("Parametrised Scenario", Meta.EMPTY));
        ExamplesTable table = new ExamplesTable("|money|to|\n|$30|Mauro|\n|$50|Paul|\n");
        reporter.beforeExamples(asList("Given money <money>", "Then I give it to <to>"), table);
        reporter.example(table.getRow(0), 0);
        reportScenarioStep(reporter, scenarioStep, Stage.BEFORE);
        reporter.beforeScenarioSteps(null);
        reporter.successful("Given money $30");
        reporter.successful("Then I give it to Mauro");
        reporter.afterScenarioSteps(null);
        reportScenarioStep(reporter, scenarioStep, Stage.AFTER);
        reporter.example(table.getRow(1), 1);
        reportScenarioStep(reporter, scenarioStep, Stage.BEFORE);
        reporter.beforeScenarioSteps(null);
        reporter.successful("Given money $50");
        reporter.successful("Then I give it to Paul");
        if (withFailure) {
            reporter.failed("Then I should have a balance of $30", new UUIDExceptionWrapper(new Exception("Expected <30> got <25>")));
        } else {
            reporter.pending("Then I should have a balance of $30");
        }
        reporter.afterScenarioSteps(null);
        reportScenarioStep(reporter, scenarioStep, Stage.AFTER);
        reporter.afterExamples();
        reporter.afterScenario();
        reporter.afterScenarios();

        reporter.beforeStorySteps(Stage.AFTER);
        reporter.beforeStep(afterStoryStep);
        reporter.successful(afterStoryStep);
        reporter.afterStorySteps(Stage.AFTER);

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

    private static void reportScenarioStep(StoryReporter reporter, String step, Stage stage) {
        reporter.beforeScenarioSteps(stage);
        reporter.beforeStep(step);
        reporter.successful(step);
        reporter.afterScenarioSteps(stage);
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
                Arrays.asList(new Scenario("A scenario", Meta.EMPTY, GivenStories.EMPTY, ExamplesTable.EMPTY, new ArrayList<String>())));
        reporter.beforeStory(story, false);
        if (storyNotAllowed) {
            reporter.storyNotAllowed(story, "-theme testing");
        } else  {
            Scenario scenario = story.getScenarios().get(0);
            reporter.beforeScenario(scenario);
            reporter.scenarioNotAllowed(scenario, "-theme testing");
            reporter.afterScenario();
        }
        reporter.afterStory(false);
    }
}
