package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hamcrest.core.IsEqual;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.failures.RestartingScenarioFailure;
import org.jbehave.core.failures.RestartingStoryFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Lifecycle.ExecutionType;
import org.jbehave.core.model.Lifecycle.Steps;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;

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
        Lifecycle lifecycle = new Lifecycle(asList(beforeScenarioSteps, beforeStorySteps),
                asList(afterScenarioSteps, afterStorySteps));
        Story story = spyStoryUuid(
                new Story("/path/to/story", new Description("An interesting story & special chars"), new Meta(meta),
                        new Narrative("renovate my house", "customer", "get a loan"), GivenStories.EMPTY, lifecycle,
                        new ArrayList<Scenario>()));
        boolean givenStory = false;

        reporter.dryRun();
        reporter.beforeStory(story, givenStory);
        reporter.narrative(story.getNarrative());
        reporter.lifecycle(lifecycle);

        reportStoryStep(reporter, beforeStoryStep, Stage.BEFORE, ExecutionType.SYSTEM);
        reportStoryStep(reporter, beforeStoryStep, Stage.BEFORE, ExecutionType.USER);

        reporter.beforeScenarios();
        reporter.beforeScenario(spyScenarioUuid(new Scenario("I ask for a loan", Meta.EMPTY)));
        reportScenarioStep(reporter, scenarioStep, Stage.BEFORE, ExecutionType.SYSTEM);
        reportScenarioStep(reporter, scenarioStep, Stage.BEFORE, ExecutionType.USER);
        reporter.beforeGivenStories();
        reporter.givenStories(asList("/given/story1", "/given/story2"));
        reporter.afterGivenStories();
        reporter.beforeScenarioSteps(null, null);
        reportSuccessfulStep(reporter, "Given I have a balance of $50");
        String ignorable = "!-- Then ignore me";
        reporter.beforeStep(new Step(StepExecutionType.IGNORABLE, ignorable));
        reporter.ignorable(ignorable);
        String comment = "!-- A comment";
        reporter.beforeStep(new Step(StepExecutionType.COMMENT, comment));
        reporter.comment(comment);
        reportCompositeStep(reporter);
        reportSuccessfulStep(reporter, "When I request $20");
        reportSuccessfulStep(reporter, "When I ask Liz for a loan of $100");
        reportSuccessfulStep(reporter, "When I ask Liz for a loan of $" + StepCreator.PARAMETER_VALUE_START + "99"
                + StepCreator.PARAMETER_VALUE_END);
        reportSuccessfulStep(reporter, "When I write special chars <>&\"");
        reportSuccessfulStep(reporter,
                "When I write special chars in parameter " + StepCreator.PARAMETER_VALUE_START + "<>&\""
                        + StepCreator.PARAMETER_VALUE_END);
        reportSuccessfulStep(reporter, "When I write two parameters "
                + StepCreator.PARAMETER_VALUE_START + ",,," + StepCreator.PARAMETER_VALUE_END
                + " and "
                + StepCreator.PARAMETER_VALUE_START + "&&&" + StepCreator.PARAMETER_VALUE_END);
        String restarted = "Then I should... - try again";
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, restarted));
        reporter.restarted(restarted, new RestartingScenarioFailure("hi")); // !
        reporter.restartedStory(story, new RestartingStoryFailure("Restarted Story"));
        reporter.storyCancelled(story, new StoryDuration(1).setDurationInSecs(2));
        if (withFailure) {
            reportFailedStep(reporter, "Then I should have a balance of $30",
                    new UUIDExceptionWrapper(new Exception("Expected <30> got <25>")));
        } else {
            reportPendingStep(reporter, "Then I should have a balance of $30");
        }
        String notPerformed = "Then I should have $20";
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, notPerformed));
        reporter.notPerformed(notPerformed);
        OutcomesTable outcomesTable = new OutcomesTable(new LocalizedKeywords(),  mapOf(Date.class, "dd/MM/yyyy"));
        outcomesTable.addOutcome("I don't return all", 100.0, equalTo(50.));
        Date actualDate = dateFor("01/01/2011");
        Date expectedDate = dateFor("02/01/2011");
        outcomesTable.addOutcome("A wrong date", actualDate, new IsDateEqual(expectedDate, outcomesTable.getFormat(
            Date.class)));
        try {
            outcomesTable.verify();
        } catch (UUIDExceptionWrapper e) {
            String failedOutcomes = "Then I don't return loan";
            reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, failedOutcomes));
            reporter.failedOutcomes(failedOutcomes, ((OutcomesFailed) e.getCause()).outcomesTable());
        }
        reporter.afterScenarioSteps(null, null);
        reportScenarioStep(reporter, scenarioStep, Stage.AFTER, ExecutionType.USER);
        reportScenarioStep(reporter, scenarioStep, Stage.AFTER, ExecutionType.SYSTEM);
        reporter.afterScenario(getTiming());
        reporter.beforeScenario(spyScenarioUuid(new Scenario("Parametrised Scenario", Meta.EMPTY)));
        ExamplesTable table = new ExamplesTable("|money|to|\n|$30|Mauro|\n|$50|Paul|\n");
        reporter.beforeExamples(asList("Given money <money>", "Then I give it to <to>"), table);
        reporter.example(table.getRow(0), 0);
        reportScenarioStep(reporter, scenarioStep, Stage.BEFORE, ExecutionType.SYSTEM);
        reportScenarioStep(reporter, scenarioStep, Stage.BEFORE, ExecutionType.USER);
        reporter.beforeScenarioSteps(null, null);
        reportSuccessfulStep(reporter, "Given money $30");
        reportSuccessfulStep(reporter, "Then I give it to Mauro");
        reporter.afterScenarioSteps(null, null);
        reportScenarioStep(reporter, scenarioStep, Stage.AFTER, ExecutionType.USER);
        reportScenarioStep(reporter, scenarioStep, Stage.AFTER, ExecutionType.SYSTEM);
        reporter.example(table.getRow(1), 1);
        reportScenarioStep(reporter, scenarioStep, Stage.BEFORE, ExecutionType.SYSTEM);
        reportScenarioStep(reporter, scenarioStep, Stage.BEFORE, ExecutionType.USER);
        reporter.beforeScenarioSteps(null, null);
        reportSuccessfulStep(reporter, "Given money $50");
        reportSuccessfulStep(reporter, "Then I give it to Paul");
        if (withFailure) {
            reportFailedStep(reporter, "Then I should have a balance of $30",
                    new UUIDExceptionWrapper(new Exception("Expected <30> got <25>"))); // !
        } else {
            reportPendingStep(reporter, "Then I should have a balance of $30");
        }
        reporter.afterScenarioSteps(null, null);
        reportScenarioStep(reporter, scenarioStep, Stage.AFTER, ExecutionType.USER);
        reportScenarioStep(reporter, scenarioStep, Stage.AFTER, ExecutionType.SYSTEM);
        reporter.afterExamples();
        reporter.afterScenario(getTiming());
        reporter.afterScenarios();

        reportStoryStep(reporter, afterStoryStep, Stage.AFTER, ExecutionType.USER);
        reportStoryStep(reporter, afterStoryStep, Stage.AFTER, ExecutionType.SYSTEM);
        reporter.afterStory(givenStory);
    }

    private static Map<Type,String> mapOf(Type type, String value) {
        Map<Type,String> map = new HashMap<>();
        map.put(type, value);
        return map;
    }

    private static void reportScenarioStep(StoryReporter reporter, String step, Stage stage,
            Lifecycle.ExecutionType type) {
        reporter.beforeScenarioSteps(stage, type);
        reportSuccessfulStep(reporter, step);
        reporter.afterScenarioSteps(stage, type);
    }

    private static void reportStoryStep(StoryReporter reporter, String step, Stage stage,
            Lifecycle.ExecutionType type) {
        reporter.beforeStorySteps(stage, type);
        reportSuccessfulStep(reporter, step);
        reporter.afterStorySteps(stage, type);
    }

    private static void reportPendingStep(StoryReporter reporter, String step) {
        reporter.beforeStep(new Step(StepExecutionType.PENDING, step));
        reporter.pending((StepCreator.PendingStep) StepCreator.createPendingStep(step, null));
    }

    private static void reportSuccessfulStep(StoryReporter reporter, String step) {
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, step));
        reporter.successful(step);
    }

    private static void reportFailedStep(StoryReporter reporter, String step, Throwable cause) {
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, step));
        reporter.failed(step, cause);
    }

    private static void reportCompositeStep(StoryReporter reporter) {
        String compositeStep = "When I perform composite step";
        String innerStep = "When I perform inner step";

        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, compositeStep));
        reporter.beforeComposedSteps();
        reportSuccessfulStep(reporter, innerStep);
        reporter.beforeStep(new Step(StepExecutionType.EXECUTABLE, compositeStep));
        reporter.beforeComposedSteps();
        reportSuccessfulStep(reporter, innerStep);
        reportSuccessfulStep(reporter, innerStep);
        reporter.afterComposedSteps();
        reporter.successful(compositeStep);
        reportSuccessfulStep(reporter, innerStep);
        reporter.afterComposedSteps();
        reporter.successful(compositeStep);
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

    static void narrateAnInterestingStoryExcludedByFilter(StoryReporter reporter, boolean storyExluded) {
        Properties meta = new Properties();
        meta.setProperty("theme", "testing");
        meta.setProperty("author", "Mauro");
        Scenario scenario = new Scenario("A scenario", Meta.EMPTY, GivenStories.EMPTY, ExamplesTable.EMPTY,
                new ArrayList<>());
        Story story = spyStoryUuid(new Story("/path/to/story", new Description("An interesting story"), new Meta(meta),
                new Narrative("renovate my house", "customer", "get a loan"), asList(scenario)));
        reporter.beforeStory(story, false);
        if (storyExluded) {
            reporter.storyExcluded(story, "-theme testing");
        } else  {
            reporter.beforeScenario(spyScenarioUuid(scenario));
            reporter.scenarioExcluded(scenario, "-theme testing");
            reporter.afterScenario(getTiming());
        }
        reporter.afterStory(false);
    }

    private static Scenario spyScenarioUuid(Scenario scenario) {
        Scenario spy = spy(scenario);
        when(spy.getId()).thenReturn("scenario-id");
        return spy;
    }

    private static Story spyStoryUuid(Story story) {
        Story spy = spy(story);
        when(spy.getId()).thenReturn("story-id");
        return spy;
    }

    private static Timing getTiming() {
        Timing timing = mock(Timing.class);
        when(timing.getStart()).thenReturn(1L);
        when(timing.getEnd()).thenReturn(2L);
        return timing;
    }
}
