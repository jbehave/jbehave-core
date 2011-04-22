package org.jbehave.core.embedder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.ConcurrentStoryReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepResult;
import org.jbehave.core.steps.Steps;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import static java.util.Arrays.asList;
import static org.jbehave.core.steps.AbstractStepResult.failed;
import static org.jbehave.core.steps.AbstractStepResult.notPerformed;
import static org.jbehave.core.steps.AbstractStepResult.pending;
import static org.jbehave.core.steps.AbstractStepResult.successful;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StoryRunnerBehaviour {

    private Map<String, String> parameters = new HashMap<String, String>();

    @Test
    public void shouldRunStepsBeforeAndAfterStories() throws Throwable {
        // Given
        Step beforeStep = mock(Step.class, "beforeStep");
        StepResult beforeResult = mock(StepResult.class);
        when(beforeStep.perform(null)).thenReturn(beforeResult);
        Step afterStep = mock(Step.class, "afterStep");
        StepResult afterResult = mock(StepResult.class);
        when(afterStep.perform(null)).thenReturn(afterResult);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        StoryReporter reporter = mock(StoryReporter.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);

        // When
        StoryRunner runner = new StoryRunner();
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.BEFORE)).thenReturn(asList(beforeStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.BEFORE);
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.AFTER)).thenReturn(asList(afterStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.AFTER);

        // Then
        verify(beforeStep).perform(null);
        verify(afterStep).perform(null);
    }

    @Test
    public void shouldReportFailuresInStepsBeforeAndAfterStories() throws Throwable {
        // Given
        Step beforeStep = mock(Step.class, "beforeStep");
        StepResult beforeResult = mock(StepResult.class, "beforeStep");
        when(beforeStep.perform(null)).thenReturn(beforeResult);
        UUIDExceptionWrapper failure = new UUIDExceptionWrapper("failed");
        when(beforeResult.getFailure()).thenReturn(failure);
        Step afterStep = mock(Step.class, "afterStep");
        StepResult afterResult = mock(StepResult.class);
        when(afterStep.doNotPerform(failure)).thenReturn(afterResult);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        StoryReporter reporter = mock(StoryReporter.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);

        // When
        StoryRunner runner = new StoryRunner();
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.BEFORE)).thenReturn(asList(beforeStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.BEFORE);
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.AFTER)).thenReturn(asList(afterStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.AFTER);

        // Then
        verify(beforeStep).perform(null);
        verify(afterStep).doNotPerform(failure);
    }

    @Test
    public void shouldRunStepsInStoryAndReportResultsToReporter() throws Throwable {
        // Given
        Scenario scenario1 = new Scenario("my title 1", asList("failingStep",
                "successfulStep"));
        Scenario scenario2 = new Scenario("my title 2", asList("successfulStep"));
        Scenario scenario3 = new Scenario("my title 3", asList("successfulStep",
                "pendingStep"));
        Story story = new Story(new Description("my blurb"), Narrative.EMPTY, asList(scenario1,
                scenario2, scenario3));
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        UUIDExceptionWrapper failure = new UUIDExceptionWrapper(new IllegalArgumentException());
        Step pendingStep = mock(Step.class, "pendingStep");
        Step successfulStep = mock(Step.class, "successfulStep");
        Step failingStep = mock(Step.class, "failingStep");
        when(successfulStep.perform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(successful("successfulStep"));
        when(successfulStep.doNotPerform(failure)).thenReturn(notPerformed("successfulStep"));
        when(pendingStep.perform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(pending("pendingStep"));
        when(pendingStep.doNotPerform(failure)).thenReturn(pending("pendingStep"));
        when(failingStep.perform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(failed("failingStep", failure));
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, parameters)).thenReturn(
                asList(failingStep, successfulStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, parameters)).thenReturn(asList(successfulStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario3, parameters)).thenReturn(
                asList(successfulStep, pendingStep));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector, failureStrategy), asList(mySteps), story);

        // Then
        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).beforeStory(story, false);
        inOrder.verify(reporter).beforeScenario("my title 1");
        inOrder.verify(reporter).failed("failingStep", failure);
        inOrder.verify(reporter).notPerformed("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 2");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 3");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).pending("pendingStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(false);
        inOrder.verify(failureStrategy).handleFailure(failure);
    }
    
    @Test
    public void shouldRunGivenStoriesBeforeSteps() throws Throwable {
        // Given
        Scenario scenario1 = new Scenario("scenario 1", asList("successfulStep"));
        GivenStories givenStories = new GivenStories("/path/to/given/story1");
        Scenario scenario2 = new Scenario("scenario 2", Meta.EMPTY, givenStories, ExamplesTable.EMPTY, 
                asList("anotherSuccessfulStep"));
        Story story1 = new Story(new Description("story 1"), Narrative.EMPTY, asList(scenario1));
        Story story2 = new Story(new Description("story 2"), Narrative.EMPTY, asList(scenario2));

        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform(null)).thenReturn(result);

        StoryParser storyParser = mock(StoryParser.class);
        StoryLoader storyLoader = mock(StoryLoader.class);
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Step successfulStep = mockSuccessfulStep("successfulStep");
        Step anotherSuccessfulStep = mockSuccessfulStep("anotherSuccessfulStep");
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story1, givenStory, collector, mySteps);
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, parameters)).thenReturn(asList(successfulStep));
        givenStoryWithNoBeforeOrAfterSteps(story2, givenStory, collector, mySteps);
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, parameters)).thenReturn(
                asList(anotherSuccessfulStep));
        when(storyLoader.loadStoryAsText("/path/to/given/story1")).thenReturn("storyContent");
        when(storyParser.parseStory("storyContent", "/path/to/given/story1")).thenReturn(story1);
        givenStoryWithNoBeforeOrAfterSteps(story1, givenStory, collector, mySteps);
        givenStoryWithNoBeforeOrAfterSteps(story2, givenStory, collector, mySteps);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);

        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWith(storyParser, storyLoader, reporter, collector, failureStrategy);
        runner.run(configuration, asList(mySteps), story2);

        // Then
        InOrder inOrder = inOrder(reporter);
        inOrder.verify(reporter).beforeStory(story2, givenStory);
        inOrder.verify(reporter).givenStories(givenStories);
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).successful("anotherSuccessfulStep");
        inOrder.verify(reporter).afterStory(givenStory);
        verify(reporter, never()).beforeStory(story1, givenStory);
    }

    @Test
    public void shouldNotPerformStepsAfterFailedOrPendingSteps() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        Step firstStepNormal = mockSuccessfulStep("Given I succeed");
        Step secondStepPending = mock(Step.class, "secondStepPending");
        Step thirdStepNormal = mock(Step.class, "thirdStepNormal");
        Step fourthStepAlsoPending = mock(Step.class, "fourthStepAlsoPending");
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Scenario scenario = new Scenario();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), eq(scenario), eq(parameters))).thenReturn(
                asList(firstStepNormal, secondStepPending, thirdStepNormal, fourthStepAlsoPending));
        when(secondStepPending.perform(null)).thenReturn(pending("When I am pending"));
        when(thirdStepNormal.doNotPerform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(notPerformed("Then I should not be performed"));
        when(fourthStepAlsoPending.doNotPerform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(
        		notPerformed("Then I should not be performed either"));
        Story story = new Story(asList(scenario));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector), asList(mySteps), story);

        // Then
        verify(firstStepNormal).perform(null);
        verify(secondStepPending).perform(null);
        verify(thirdStepNormal).doNotPerform(Matchers.<UUIDExceptionWrapper>any());
        verify(fourthStepAlsoPending).doNotPerform(Matchers.<UUIDExceptionWrapper>any());

        verify(reporter).successful("Given I succeed");
        verify(reporter).pending("When I am pending");
        verify(reporter).notPerformed("Then I should not be performed");
        verify(reporter).notPerformed("Then I should not be performed either");
    }

    @Test
    public void shouldReportAnyFailuresAndHandleThemAfterStory() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        Step firstStepExceptional = mock(Step.class);
        Step secondStepNotPerformed = mock(Step.class);
        StepResult failed = failed("When I fail", new UUIDExceptionWrapper(new IllegalStateException()));
        StepResult notPerformed = notPerformed("Then I should not be performed");
        when(firstStepExceptional.perform(null)).thenReturn(failed);
        when(secondStepNotPerformed.doNotPerform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(notPerformed);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Scenario scenario = new Scenario();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), eq(scenario), eq(parameters))).thenReturn(
                asList(firstStepExceptional, secondStepNotPerformed));
        Story story = new Story(asList(scenario));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector, failureStrategy), asList(mySteps), story);

        // Then
        verify(firstStepExceptional).perform(null);
        verify(secondStepNotPerformed).doNotPerform(Matchers.<UUIDExceptionWrapper>any());

        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).beforeStory((Story) anyObject(), eq(givenStory));
        inOrder.verify(reporter).beforeScenario((String) anyObject());
        inOrder.verify(reporter).failed("When I fail", failed.getFailure());
        inOrder.verify(reporter).notPerformed("Then I should not be performed");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(givenStory);
        inOrder.verify(failureStrategy).handleFailure(failed.getFailure());
    }

    @Test
    public void shouldAllowToSkipScenariosAfterFailedScenario() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        Step failedStep = mock(Step.class);
        Step neverExecutedStep = mock(Step.class);
        StepResult failed = failed("When I fail", new UUIDExceptionWrapper(new IllegalStateException()));
        when(failedStep.perform(null)).thenReturn(failed);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Scenario scenario1 = new Scenario();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), eq(scenario1), eq(parameters))).thenReturn(
                asList(failedStep));
        Scenario scenario2 = new Scenario();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), eq(scenario2), eq(parameters))).thenReturn(
                asList(neverExecutedStep));
        Story story = new Story(asList(scenario1, scenario2));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWith(reporter, collector, failureStrategy);
        configuration.storyControls().doSkipScenariosAfterFailure(true);
        runner.run(configuration, asList(mySteps), story);

        // Then
        verify(failedStep).perform(null);
        verify(neverExecutedStep, never()).perform(null);

    }

    @Test
    public void shouldAllowToSkipBeforeAndAfterScenarioStepsIfGivenStory() throws Throwable {
        // Given
        Scenario scenario1 = new Scenario("scenario 1", asList("successfulStep"));
        GivenStories givenStories = new GivenStories("/path/to/given/story1");
        Scenario scenario2 = new Scenario("scenario 2", Meta.EMPTY, givenStories, ExamplesTable.EMPTY,
                asList("anotherSuccessfulStep"));
        Story story1 = new Story(new Description("story 1"), Narrative.EMPTY, asList(scenario1));
        Story story2 = new Story(new Description("story 2"), Narrative.EMPTY, asList(scenario2));

        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform(null)).thenReturn(result);

        StoryParser storyParser = mock(StoryParser.class);
        StoryLoader storyLoader = mock(StoryLoader.class);
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Step successfulStep = mockSuccessfulStep("successfulStep");
        Step anotherSuccessfulStep = mockSuccessfulStep("anotherSuccessfulStep");
        givenStoryWithNoBeforeOrAfterSteps(story1, false, collector, mySteps);
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, parameters)).thenReturn(asList(successfulStep));
        givenStoryWithNoBeforeOrAfterSteps(story2, true, collector, mySteps);
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, parameters)).thenReturn(
                asList(anotherSuccessfulStep));
        when(storyLoader.loadStoryAsText("/path/to/given/story1")).thenReturn("storyContent");
        when(storyParser.parseStory("storyContent", "/path/to/given/story1")).thenReturn(story1);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);

        Step beforeStep = mockSuccessfulStep("SuccessfulBeforeScenarioStep");
        Step afterStep = mockSuccessfulStep("SuccessfulAfterScenarioStep");
        boolean failureOccured = false;
        when(collector.collectBeforeOrAfterScenarioSteps(asList(mySteps), Stage.BEFORE, failureOccured)).thenReturn(asList(beforeStep));
        when(collector.collectBeforeOrAfterScenarioSteps(asList(mySteps), Stage.AFTER, failureOccured)).thenReturn(asList(afterStep));

        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWith(storyParser, storyLoader, reporter, collector, failureStrategy);
        configuration.storyControls().doSkipBeforeAndAfterScenarioStepsIfGivenStory(true);
        runner.run(configuration, asList(mySteps), story2);

        // Then
        verify(collector).collectScenarioSteps(asList(mySteps), scenario1, parameters);
        verify(collector).collectScenarioSteps(asList(mySteps), scenario2, parameters);

        InOrder inOrder = inOrder(beforeStep, successfulStep, anotherSuccessfulStep, afterStep);

        inOrder.verify(beforeStep).perform(null);
        inOrder.verify(successfulStep).perform(null);
        inOrder.verify(anotherSuccessfulStep).perform(null);
        inOrder.verify(afterStep).perform(null);
    }

    @Test
    public void shouldResetStateBeforeScenario() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        Step pendingStep = mock(Step.class);
        when(pendingStep.perform(null)).thenReturn(pending("pendingStep"));
        Step secondStep = mockSuccessfulStep("secondStep");
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Scenario scenario1 = new Scenario();
        Scenario scenario2 = new Scenario();
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, parameters)).thenReturn(asList(pendingStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, parameters)).thenReturn(asList(secondStep));
        Story story = new Story(asList(scenario1, scenario2));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWith(reporter, collector);
        configuration.storyControls().doResetStateBeforeScenario(true);
        runner.run(configuration, asList(mySteps), story);

        // Then
        verify(pendingStep).perform(Matchers.<UUIDExceptionWrapper>any());
        verify(secondStep).perform(Matchers.<UUIDExceptionWrapper>any());
        verify(secondStep, never()).doNotPerform(Matchers.<UUIDExceptionWrapper>any());
    }

    @Test
    public void shouldAllowToNotResetStateBeforeScenario() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        Step pendingStep = mock(Step.class);
        when(pendingStep.perform(null)).thenReturn(pending("pendingStep"));
        Step secondStep = mockSuccessfulStep("secondStep");
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Scenario scenario1 = new Scenario();
        Scenario scenario2 = new Scenario();
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, parameters)).thenReturn(asList(pendingStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, parameters)).thenReturn(asList(secondStep));
        Story story = new Story(asList(scenario1, scenario2));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWith(reporter, collector);
        configuration.storyControls().doResetStateBeforeScenario(false);
        runner.run(configuration, asList(mySteps), story);

        // Then
        verify(pendingStep).perform(Matchers.<UUIDExceptionWrapper>any());
        verify(secondStep).doNotPerform(Matchers.<UUIDExceptionWrapper>any());
        verify(secondStep, never()).perform(Matchers.<UUIDExceptionWrapper>any());
    }

    @Test
    public void shouldRunBeforeAndAfterStorySteps() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        Step beforeStep = mockSuccessfulStep("beforeStep");
        Step afterStep = mockSuccessfulStep("secondStep");
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Story story = new Story();
        boolean givenStory = false;
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.BEFORE, givenStory)).thenReturn(asList(beforeStep));
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.AFTER, givenStory)).thenReturn(asList(afterStep));

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector),asList(mySteps), story);

        // Then
        verify(beforeStep).perform(null);
        verify(afterStep).perform(null);
    }

    @Test
    public void shouldHandlePendingStepsAccordingToStrategy() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        Step pendingStep = mock(Step.class);
        StepResult pendingResult = pending("My step isn't defined!");
        when(pendingStep.perform(null)).thenReturn(pendingResult);
        PendingStepStrategy strategy = mock(PendingStepStrategy.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(parameters))).thenReturn(
                asList(pendingStep));
        Story story = new Story(asList(new Scenario()));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWithPendingStrategy(collector, reporter,
                strategy), asList(mySteps), story);

        // Then
        verify(strategy).handleFailure(pendingResult.getFailure());
    }

    @Test(expected = PendingStepFound.class)
    public void shouldFailWithFailingUpongPendingStepsStrategy() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        Step pendingStep = mock(Step.class);
        StepResult pendingResult = pending("My step isn't defined!");
        when(pendingStep.perform(null)).thenReturn(pendingResult);
        PendingStepStrategy strategy = new FailingUponPendingStep();
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(parameters))).thenReturn(
                asList(pendingStep));
        Story story = new Story(asList(new Scenario()));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWithPendingStrategy(collector, reporter,
                strategy), asList(mySteps), story);

        // Then ... fail as expected
    }

    @Test
    public void shouldRunScenarioWithExamplesTable() throws Throwable {
        // Given
        ExamplesTable examplesTable = new ExamplesTable("|one|two|\n|1|2|\n");
        Map<String, String> tableRow = examplesTable.getRow(0);
        Scenario scenario1 = new Scenario("my title 1", Meta.EMPTY, GivenStories.EMPTY, examplesTable, asList("step <one>",
                "step <two>"));
        Story story = new Story(new Description("my blurb"), Narrative.EMPTY, asList(scenario1));
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform(null)).thenReturn(result);
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        Configuration configuration = configurationWith(reporter, collector, failureStrategy);
        configuration.storyControls().doDryRun(true);
        CandidateSteps mySteps = new Steps(configuration);
        Step firstStep = mockSuccessfulStep("step <one>");
        Step secondStep = mockSuccessfulStep("step <two>");
        when(collector.collectScenarioSteps(asList(mySteps), scenario1,tableRow)).thenReturn(
                asList(firstStep, secondStep));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configuration, asList(mySteps), story);

        // Then
        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).beforeStory(story, givenStory);
        inOrder.verify(reporter).beforeScenario("my title 1");
        inOrder.verify(reporter).successful("step <one>");
        inOrder.verify(reporter).successful("step <two>");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(givenStory);
    }


    @Test
    public void shouldRunStepsInDryRunMode() throws Throwable {
        // Given
        Scenario scenario1 = new Scenario("my title 1", asList("failingStep",
                "successfulStep"));
        Scenario scenario2 = new Scenario("my title 2", asList("successfulStep"));
        Scenario scenario3 = new Scenario("my title 3", asList("successfulStep",
                "pendingStep"));
        Story story = new Story(new Description("my blurb"), Narrative.EMPTY, asList(scenario1,
                scenario2, scenario3));
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class, "result");
        when(step.perform(null)).thenReturn(result);
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        Configuration configuration = configurationWith(reporter, collector, failureStrategy);
        configuration.doDryRun(true);
        CandidateSteps mySteps = new Steps(configuration);
        UUIDExceptionWrapper failure = new UUIDExceptionWrapper(new IllegalArgumentException());
        Step successfulStep = mockSuccessfulStep("successfulStep");
        Step pendingStep = mock(Step.class, "pendingStep");
        Step failingStep = mock(Step.class, "failingStep");
        when(pendingStep.perform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(pending("pendingStep"));
        when(pendingStep.doNotPerform(failure)).thenReturn(pending("pendingStep"));
        when(failingStep.perform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(failed("failingStep", failure));
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, parameters)).thenReturn(
                asList(failingStep, successfulStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, parameters)).thenReturn(asList(successfulStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario3, parameters)).thenReturn(
                asList(successfulStep, pendingStep));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configuration, asList(mySteps), story);

        // Then
        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).beforeStory(story, givenStory);
        inOrder.verify(reporter).beforeScenario("my title 1");
        inOrder.verify(reporter).failed("failingStep", failure);
        inOrder.verify(reporter).notPerformed("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 2");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 3");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).pending("pendingStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(givenStory);
        inOrder.verify(failureStrategy).handleFailure(failure);

    }

    @Test
    public void shouldNotRunStoriesNotAllowedByFilter() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(parameters))).thenReturn(
                Arrays.<Step>asList());
        Meta meta = mock(Meta.class);
        Story story = new Story("", Description.EMPTY, meta, Narrative.EMPTY, asList(new Scenario()));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);
        MetaFilter filter = mock(MetaFilter.class);
        String filterAsString = "-some property";
        
        // When
        StoryRunner runner = new StoryRunner();
        when(filter.allow(meta)).thenReturn(false);
        when(filter.asString()).thenReturn(filterAsString);
        runner.run(configurationWith(reporter, collector), asList(mySteps), story, filter);

        // Then
        verify(reporter).beforeStory(story, givenStory);
        verify(reporter).storyNotAllowed(story, filterAsString);
        verify(reporter).afterStory(givenStory);
    }
    
    @Test
    public void shouldNotRunScenariosNotAllowedByFilter() throws Throwable {
        // Given
        StoryReporter reporter = mock(ConcurrentStoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), (Scenario) anyObject(), eq(parameters))).thenReturn(
                Arrays.<Step>asList());
        Meta meta = mock(Meta.class);
        Meta storyMeta = mock(Meta.class);
        Story story = new Story("", Description.EMPTY, storyMeta, Narrative.EMPTY, asList(new Scenario("", meta, GivenStories.EMPTY, ExamplesTable.EMPTY, asList(""))));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);
        MetaFilter filter = mock(MetaFilter.class);
        String filterAsString = "-some property";

        // When
        StoryRunner runner = new StoryRunner();
        when(filter.allow(storyMeta)).thenReturn(true);
        when(meta.inheritFrom(storyMeta)).thenReturn(meta);
        when(filter.allow(meta)).thenReturn(false);
        when(filter.asString()).thenReturn(filterAsString);
        runner.run(configurationWith(reporter, collector), asList(mySteps), story, filter);

        // Then
        verify(reporter).beforeStory(story, givenStory);
        verify(reporter).beforeScenario("");
        verify(reporter).scenarioNotAllowed(story.getScenarios().get(0), filterAsString);
        verify(reporter).afterScenario();
    }
    
    private void givenStoryWithNoBeforeOrAfterSteps(Story story, boolean givenStory, StepCollector collector, CandidateSteps mySteps) {
        List<Step> steps = asList();
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.BEFORE, givenStory)).thenReturn(steps);
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.AFTER, givenStory)).thenReturn(steps);
    }

    private Configuration configurationWithPendingStrategy(StepCollector collector, StoryReporter reporter,
                                                                PendingStepStrategy strategy) {
        return configurationWith(new RegexStoryParser(), new LoadFromClasspath(), reporter, collector,
                new RethrowingFailure(), strategy);
    }

    private Configuration configurationWith(final StoryReporter reporter, final StepCollector collector) {
        return configurationWith(reporter, collector, new RethrowingFailure());
    }

    private Configuration configurationWith(StoryReporter reporter, StepCollector collector, FailureStrategy failureStrategy) {
        return configurationWith(new RegexStoryParser(), new LoadFromClasspath(), reporter, collector, failureStrategy);
    }

    private Configuration configurationWith(StoryParser parser, final StoryLoader storyLoader, final StoryReporter reporter,
                                                 final StepCollector collector, final FailureStrategy failureStrategy) {
        return configurationWith(parser, storyLoader, reporter, collector, failureStrategy, new PassingUponPendingStep());
    }

    private Configuration configurationWith(final StoryParser parser, final StoryLoader loader, final StoryReporter reporter,
                                                 final StepCollector collector, final FailureStrategy failureStrategy, final PendingStepStrategy pendingStrategy) {

        return new MostUsefulConfiguration() {
            @Override
            public StoryReporter storyReporter(String storyPath) {
                return reporter;
            }
        }.useStoryParser(parser)
                .useStoryLoader(loader)
                .useStepCollector(collector)
                .useFailureStrategy(failureStrategy)
                .usePendingStepStrategy(pendingStrategy);
    }

    private Step mockSuccessfulStep(String result) {
        Step step = mock(Step.class, result);        
        when(step.perform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(successful(result));
        when(step.doNotPerform(Matchers.<UUIDExceptionWrapper>any())).thenReturn(notPerformed("successfulStep"));
        return step;
    }
    
}
