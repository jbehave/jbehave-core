package org.jbehave.core.embedder;

import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryRunner.State;
import org.jbehave.core.failures.*;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.*;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.*;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.AbstractStep;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.steps.AbstractStepResult.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class StoryRunnerBehaviour {

    private Map<String, String> parameters = new HashMap<>();

    @Test
    public void shouldRunStepsBeforeAndAfterStories() {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step beforeStep = mock(Step.class, "beforeStep");
        StepResult beforeResult = mock(StepResult.class);
        when(beforeStep.perform(reporter, null)).thenReturn(beforeResult);
        Step afterStep = mock(Step.class, "afterStep");
        StepResult afterResult = mock(StepResult.class);
        when(afterStep.perform(reporter, null)).thenReturn(afterResult);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        FailureStrategy failureStrategy = mock(FailureStrategy.class);

        // When
        StoryRunner runner = new StoryRunner();
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.BEFORE)).thenReturn(asList(beforeStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.BEFORE);
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.AFTER)).thenReturn(asList(afterStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.AFTER);

        // Then
        verify(beforeStep).perform(reporter, null);
        verify(afterStep).perform(reporter, null);
    }

    @Test
    public void shouldReportFailuresInStepsBeforeAndAfterStories() {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step beforeStep = mock(Step.class, "beforeStep");
        StepResult beforeResult = mock(StepResult.class, "beforeStep");
        when(beforeStep.perform(reporter, null)).thenReturn(beforeResult);
        UUIDExceptionWrapper failure = new UUIDExceptionWrapper("failed");
        when(beforeResult.getFailure()).thenReturn(failure);
        Step afterStep = mock(Step.class, "afterStep");
        StepResult afterResult = mock(StepResult.class);
        when(afterStep.doNotPerform(reporter, failure)).thenReturn(afterResult);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        FailureStrategy failureStrategy = mock(FailureStrategy.class);

        // When
        StoryRunner runner = new StoryRunner();
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.BEFORE)).thenReturn(asList(beforeStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.BEFORE);
        when(collector.collectBeforeOrAfterStoriesSteps(asList(mySteps), Stage.AFTER)).thenReturn(asList(afterStep));
        runner.runBeforeOrAfterStories(configurationWith(reporter, collector, failureStrategy), asList(mySteps), Stage.AFTER);

        // Then
        verify(beforeStep).perform(reporter, null);
        verify(afterStep).doNotPerform(reporter, failure);
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
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        UUIDExceptionWrapper failure = new UUIDExceptionWrapper(new IllegalArgumentException());
        Step pendingStep = mock(Step.class, "pendingStep");
        Step successfulStep = mock(Step.class, "successfulStep");
        Step failingStep = mock(Step.class, "failingStep");
        when(successfulStep.perform(eq(reporter), any())).thenReturn(successful("successfulStep"));
        when(successfulStep.doNotPerform(reporter, failure)).thenReturn(notPerformed("successfulStep"));
        when(pendingStep.perform(eq(reporter), any())).thenReturn(pending("pendingStep"));
        when(pendingStep.doNotPerform(reporter, failure)).thenReturn(pending("pendingStep"));
        when(failingStep.perform(eq(reporter), any())).thenReturn(failed("failingStep", failure));
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
    public void shouldRunGivenStoriesAtStoryAndScenarioLevel() throws Throwable {
        // Given
        GivenStories storyGivenStories = new GivenStories("/path/to/given/story1");
        GivenStories scenarioGivenStories = new GivenStories("/path/to/given/story1");
        Scenario scenario1 = new Scenario("scenario 1", asList("successfulStep"));
        Scenario scenario2 = new Scenario("scenario 2", Meta.EMPTY, scenarioGivenStories, ExamplesTable.EMPTY, 
                asList("anotherSuccessfulStep"));
        Story story1 = new Story(new Description("story 1"), Narrative.EMPTY, asList(scenario1));
        Story story2 = new Story("", new Description("story 2"), Meta.EMPTY, Narrative.EMPTY, storyGivenStories, asList(scenario2));

        StoryReporter reporter = mock(StoryReporter.class);
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform(reporter, null)).thenReturn(result);

        StoryParser storyParser = mock(StoryParser.class);
        StoryLoader storyLoader = mock(StoryLoader.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Step successfulStep = mockSuccessfulStep(reporter, "successfulStep");
        Step anotherSuccessfulStep = mockSuccessfulStep(reporter, "anotherSuccessfulStep");
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
        inOrder.verify(reporter).givenStories(storyGivenStories);
        inOrder.verify(reporter).givenStories(scenarioGivenStories);
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).successful("anotherSuccessfulStep");
        inOrder.verify(reporter).afterStory(givenStory);
        verify(reporter, never()).beforeStory(story1, givenStory);
    }

    
    @Test
	public void shouldIgnoreMetaFilteringInGivenStoriesIfConfigured() {
		// Given
		Scenario scenario = new Scenario("scenario", new Meta(
				asList("run false")), new GivenStories("/path/to/given/story"),
				ExamplesTable.EMPTY, asList("anotherSuccessfulStep"));
		Story story = new Story("", new Description("story"), new Meta(
				asList("run false")), Narrative.EMPTY, new GivenStories(
				"/path/to/given/story"), asList(scenario));

		// When
		MetaFilter filter = new MetaFilter("+run true");
		FilteredStory ignoreMeta = new FilteredStory(filter, story,
				new StoryControls().doIgnoreMetaFiltersIfGivenStory(true), true);

		// Then
		assertThat(ignoreMeta.allowed(), is(true));
		assertThat(ignoreMeta.allowed(scenario), is(true));

		// When
		FilteredStory doNotIgnoreMeta = new FilteredStory(filter, story,
				new StoryControls().doIgnoreMetaFiltersIfGivenStory(false), true);

		// Then
		assertThat(doNotIgnoreMeta.allowed(), is(false));
		assertThat(doNotIgnoreMeta.allowed(scenario), is(false));

    }

    @Test
    public void shouldNotPerformStepsAfterFailedOrPendingSteps() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step firstStepNormal = mockSuccessfulStep(reporter, "Given I succeed");
        Step secondStepPending = mock(Step.class, "secondStepPending");
        Step thirdStepNormal = mock(Step.class, "thirdStepNormal");
        Step fourthStepAlsoPending = mock(Step.class, "fourthStepAlsoPending");
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Scenario scenario = new Scenario();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), eq(scenario), eq(parameters))).thenReturn(
                asList(firstStepNormal, secondStepPending, thirdStepNormal, fourthStepAlsoPending));
        when(secondStepPending.perform(reporter, null)).thenReturn(pending("When I am pending"));
        when(thirdStepNormal.doNotPerform(eq(reporter), any())).thenReturn(
                notPerformed("Then I should not be performed"));
        when(fourthStepAlsoPending.doNotPerform(eq(reporter), any())).thenReturn(
                notPerformed("Then I should not be performed either"));
        Story story = new Story(asList(scenario));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector), asList(mySteps), story);

        // Then
        verify(firstStepNormal).perform(reporter, null);
        verify(secondStepPending).perform(reporter, null);
        verify(thirdStepNormal).doNotPerform(eq(reporter), any());
        verify(fourthStepAlsoPending).doNotPerform(eq(reporter), any());

        verify(reporter).successful("Given I succeed");
        verify(reporter).pending("When I am pending");
        verify(reporter).notPerformed("Then I should not be performed");
        verify(reporter).notPerformed("Then I should not be performed either");
    }

    @Test
    public void shouldNotPerformStepsAfterRestaringScenarioFailure() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step firstStepNormal = mockSuccessfulStep(reporter, "Given I succeed");
        final RestartingScenarioFailure hi = new RestartingScenarioFailure("hi");
        Step restartStep = new AbstractStep() {
            private int count = 0;

            @Override
            public StepResult perform(StoryReporter reporter, UUIDExceptionWrapper storyFailureIfItHappened) {
                if (count == 0) {
                    count++;
                    throw hi;
                }
                return new AbstractStepResult.Successful("When happened on second attempt");
            }

            @Override
            public StepResult doNotPerform(StoryReporter reporter, UUIDExceptionWrapper storyFailureIfItHappened) {
                return null;
            }

            @Override
            public String toString() {
                return "<fooStep>";
            }
        };
        Step lastStepNormal = mockSuccessfulStep(reporter, "Then I succeeded");

        StepCollector collector = mock(StepCollector.class);
        FailureStrategy strategy = mock(FailureStrategy.class);
        CandidateSteps mySteps = new Steps();
        Scenario scenario = new Scenario();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), eq(scenario), eq(parameters))).thenReturn(
                asList(firstStepNormal, restartStep, lastStepNormal));
        Story story = new Story(asList(scenario));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector, strategy), asList(mySteps), story);

        verify(reporter, times(2)).successful("Given I succeed");
        verify(reporter).restarted(eq("<fooStep>"), isA(RestartingScenarioFailure.class));
        verify(reporter).successful("When happened on second attempt");
        verify(reporter).successful("Then I succeeded");
    }

    @Test
    public void shouldReportStoryCancellation(){
        // Given
        Configuration configuration = mock(Configuration.class,Mockito.RETURNS_DEEP_STUBS);
        when(configuration.storyControls().dryRun()).thenReturn(false);
        StoryReporter reporter = mock(StoryReporter.class);
        when(configuration.storyReporter(anyString())).thenReturn(reporter);
        
        Story story = mock(Story.class);
        String storyPath = "story/path";
        when(story.getPath()).thenReturn(storyPath);
        RuntimeException expected = new RuntimeException();
        when(story.getMeta()).thenThrow(expected);
        
        InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);
        MetaFilter metaFilter = mock(MetaFilter.class);
        State state = mock(State.class);
    
        //When
        long durationInSecs = 2;
        long timeoutInSecs = 1;
        StoryDuration storyDuration = new StoryDuration(timeoutInSecs);
        StoryRunner runner = new StoryRunner();
        runner.cancelStory(story, storyDuration);
        Throwable throwable = assertThrows(Throwable.class,
                () -> runner.run(configuration, stepsFactory, story, metaFilter, state));
        //Then
        assertThat(throwable.equals(expected), is(true));
        verify(reporter).storyCancelled(story, storyDuration);
    }

    @Test
    public void shouldReportAnyFailuresAndHandleThemAfterStory() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step firstStepExceptional = mock(Step.class);
        Step secondStepNotPerformed = mock(Step.class);
        StepResult failed = failed("When I fail", new UUIDExceptionWrapper(new IllegalStateException()));
        StepResult notPerformed = notPerformed("Then I should not be performed");
        when(firstStepExceptional.perform(reporter, null)).thenReturn(failed);
        when(secondStepNotPerformed.doNotPerform(eq(reporter), any())).thenReturn(notPerformed);
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
        verify(firstStepExceptional).perform(reporter, null);
        verify(secondStepNotPerformed).doNotPerform(eq(reporter), any());

        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).beforeStory(any(), eq(givenStory));
        inOrder.verify(reporter).beforeScenario((String) any());
        inOrder.verify(reporter).failed("When I fail", failed.getFailure());
        inOrder.verify(reporter).notPerformed("Then I should not be performed");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(givenStory);
        inOrder.verify(failureStrategy).handleFailure(failed.getFailure());
    }

    @Test
    public void shouldAllowToSkipScenariosAfterFailedScenario() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step failedStep = mock(Step.class);
        Step neverExecutedStep = mock(Step.class);
        StepResult failed = failed("When I fail", new UUIDExceptionWrapper(new IllegalStateException()));
        when(failedStep.perform(reporter, null)).thenReturn(failed);
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
        verify(failedStep).perform(reporter, null);
        verify(neverExecutedStep, never()).perform(reporter, null);

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

        StoryReporter reporter = mock(StoryReporter.class);
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform(reporter, null)).thenReturn(result);

        StoryParser storyParser = mock(StoryParser.class);
        StoryLoader storyLoader = mock(StoryLoader.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        Step successfulStep = mockSuccessfulStep(reporter, "successfulStep");
        Step anotherSuccessfulStep = mockSuccessfulStep(reporter, "anotherSuccessfulStep");
        givenStoryWithNoBeforeOrAfterSteps(story1, false, collector, mySteps);
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, parameters)).thenReturn(asList(successfulStep));
        givenStoryWithNoBeforeOrAfterSteps(story2, true, collector, mySteps);
        when(collector.collectScenarioSteps(asList(mySteps), scenario2, parameters)).thenReturn(
                asList(anotherSuccessfulStep));
        when(storyLoader.loadStoryAsText("/path/to/given/story1")).thenReturn("storyContent");
        when(storyParser.parseStory("storyContent", "/path/to/given/story1")).thenReturn(story1);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);

        Step beforeStep = mockSuccessfulStep(reporter, "SuccessfulBeforeScenarioStep");
        Step afterStep = mockSuccessfulStep(reporter, "SuccessfulAfterScenarioStep");
        when(collector.collectBeforeOrAfterScenarioSteps(eq(asList(mySteps)), any(), eq(Stage.BEFORE), eq(ScenarioType.NORMAL))).thenReturn(asList(beforeStep));
        when(collector.collectBeforeOrAfterScenarioSteps(eq(asList(mySteps)), any(), eq(Stage.AFTER), eq(ScenarioType.NORMAL))).thenReturn(asList(afterStep));

        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWith(storyParser, storyLoader, reporter, collector, failureStrategy);
        configuration.storyControls().doSkipBeforeAndAfterScenarioStepsIfGivenStory(true);
        runner.run(configuration, asList(mySteps), story2);

        // Then
        verify(collector).collectScenarioSteps(asList(mySteps), scenario1, parameters);
        verify(collector).collectScenarioSteps(asList(mySteps), scenario2, parameters);

        InOrder inOrder = inOrder(beforeStep, successfulStep, anotherSuccessfulStep, afterStep);

        inOrder.verify(beforeStep).perform(reporter, null);
        inOrder.verify(successfulStep).perform(reporter, null);
        inOrder.verify(anotherSuccessfulStep).perform(reporter, null);
        inOrder.verify(afterStep).perform(reporter, null);
    }

    @Test
    public void shouldResetStateBeforeScenario() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step pendingStep = mock(Step.class);
        when(pendingStep.perform(reporter, null)).thenReturn(pending("pendingStep"));
        Step secondStep = mockSuccessfulStep(reporter, "secondStep");
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
        verify(pendingStep).perform(eq(reporter), any());
        verify(secondStep).perform(eq(reporter), any());
        verify(secondStep, never()).doNotPerform(eq(reporter), any());
    }

    @Test
    public void shouldAllowToNotResetStateBeforeScenario() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step pendingStep = mock(Step.class);
        when(pendingStep.perform(reporter, null)).thenReturn(pending("pendingStep"));
        Step secondStep = mockSuccessfulStep(reporter, "secondStep");
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
        verify(pendingStep).perform(eq(reporter), any());
        verify(secondStep).doNotPerform(eq(reporter), any());
        verify(secondStep, never()).perform(eq(reporter), any());
    }    
    
    @Test
    public void shouldAllowToNotResetStateBeforeStory() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step failedStep = mock(Step.class, "failedStep");
        when(failedStep.perform(reporter, null)).thenReturn(failed("before stories", new UUIDExceptionWrapper(new RuntimeException("BeforeStories fail"))));
        Step pendingStep = mock(Step.class, "pendingStep");
        when(pendingStep.perform(reporter, null)).thenReturn(pending("pendingStep"));
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();        
        Scenario scenario1 = new Scenario();
        List<CandidateSteps> candidateSteps = asList(mySteps);
        when(collector.collectBeforeOrAfterStoriesSteps(candidateSteps, Stage.BEFORE)).thenReturn(asList(failedStep));
        when(collector.collectScenarioSteps(candidateSteps, scenario1, parameters)).thenReturn(asList(pendingStep));
        Story story = new Story(asList(scenario1));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWith(reporter, collector);
        configuration.storyControls().doResetStateBeforeStory(false).doResetStateBeforeScenario(false);
        runner.runBeforeOrAfterStories(configuration, candidateSteps, Stage.BEFORE);
        runner.run(configuration, candidateSteps, story);

        // Then
        verify(failedStep).perform(eq(reporter), any());
        verify(pendingStep).perform(eq(reporter), any());
    }

    @Test
    public void shouldRunBeforeAndAfterStorySteps() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step beforeStep = mockSuccessfulStep(reporter, "beforeStep");
        Step afterStep = mockSuccessfulStep(reporter, "secondStep");
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy strategy = mock(FailureStrategy.class);
        CandidateSteps mySteps = new Steps();
        Story story = new Story();
        boolean givenStory = false;
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.BEFORE, givenStory)).thenReturn(asList(beforeStep));
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.AFTER, givenStory)).thenReturn(asList(afterStep));

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector, strategy),asList(mySteps), story);

        // Then
        verify(beforeStep).perform(reporter, null);
        verify(afterStep).perform(reporter, null);
    }

    @Test
    public void shouldHandlePendingStepsAccordingToStrategy() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step pendingStep = mock(Step.class);
        StepResult pendingResult = pending("My step isn't defined!");
        when(pendingStep.perform(reporter, null)).thenReturn(pendingResult);
        PendingStepStrategy strategy = mock(PendingStepStrategy.class);
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), any(), eq(parameters))).thenReturn(
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

    @Test
    public void shouldFailWithFailingUponPendingStepsStrategy() {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step pendingStep = mock(Step.class);
        StepResult pendingResult = pending("My step isn't defined!");
        when(pendingStep.perform(reporter, null)).thenReturn(pendingResult);
        PendingStepStrategy strategy = new FailingUponPendingStep();
        StepCollector collector = mock(StepCollector.class);
        CandidateSteps mySteps = new Steps();
        List<CandidateSteps> candidateSteps = asList(mySteps);
        when(collector.collectScenarioSteps(eq(candidateSteps), any(), eq(parameters))).thenReturn(
                asList(pendingStep));
        Story story = new Story(asList(new Scenario()));
        givenStoryWithNoBeforeOrAfterSteps(story, false, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWithPendingStrategy(collector, reporter, strategy);
        assertThrows(PendingStepFound.class, () -> runner.run(configuration, candidateSteps, story));
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
        StoryReporter reporter = mock(StoryReporter.class);
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform(reporter, null)).thenReturn(result);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        Configuration configuration = configurationWith(reporter, collector, failureStrategy);
        configuration.storyControls().doDryRun(true);
        CandidateSteps mySteps = new Steps(configuration);
        Step firstStep = mockSuccessfulStep(reporter, "step <one>");
        Step secondStep = mockSuccessfulStep(reporter, "step <two>");
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
    public void shouldRunScenarioAndLifecycleStepsInCorrectOrderWithExamplesTable() throws Throwable{
        // Given
        ExamplesTable examplesTable = new ExamplesTable("|one|two|\n|1|2|\n|3|4|\n");
        Map<String, String> tableRow1 = examplesTable.getRow(0);
        Map<String, String> tableRow2 = examplesTable.getRow(1);
        Scenario scenario1 = new Scenario("my title 1", Meta.EMPTY, GivenStories.EMPTY, examplesTable, asList("step <one>",
                "step <two>"));
        Story story = new Story(new Description("my blurb"), Narrative.EMPTY, asList(scenario1));
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        Configuration configuration = configurationWith(reporter, collector, failureStrategy);
        configuration.storyControls().doDryRun(true);
        CandidateSteps mySteps = new Steps(configuration);
        Step firstStep = mockSuccessfulStep(reporter, "step <one>");
        Step secondStep = mockSuccessfulStep(reporter, "step <two>");
        when(collector.collectScenarioSteps(asList(mySteps), scenario1,tableRow1)).thenReturn(
                asList(firstStep, secondStep));
        when(collector.collectScenarioSteps(asList(mySteps), scenario1,tableRow2)).thenReturn(
                asList(firstStep, secondStep));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);

        givenBeforeAndAfterScenarioSteps(reporter, ScenarioType.NORMAL, collector, mySteps);
        givenBeforeAndAfterScenarioSteps(reporter, ScenarioType.ANY, collector, mySteps);
        givenBeforeAndAfterScenarioSteps(reporter, ScenarioType.EXAMPLE, collector, mySteps);

        givenLifecycleSteps(reporter, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configuration, asList(mySteps), story);

        // Then
        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).successful(stepNameFor(Stage.BEFORE, ScenarioType.NORMAL));

        inOrder.verify(reporter).successful(stepNameFor(Stage.BEFORE, ScenarioType.EXAMPLE));
        inOrder.verify(reporter).successful(stepNameFor(Stage.BEFORE, ScenarioType.ANY));
        inOrder.verify(reporter).successful(lifecycleStepNameFor(Stage.BEFORE));
        inOrder.verify(reporter).successful("step <one>");
        inOrder.verify(reporter).successful("step <two>");
        inOrder.verify(reporter).successful(lifecycleStepNameFor(Stage.AFTER));
        inOrder.verify(reporter).successful(stepNameFor(Stage.AFTER, ScenarioType.ANY));
        inOrder.verify(reporter).successful(stepNameFor(Stage.AFTER, ScenarioType.EXAMPLE));

        inOrder.verify(reporter).successful(stepNameFor(Stage.BEFORE, ScenarioType.EXAMPLE));
        inOrder.verify(reporter).successful(stepNameFor(Stage.BEFORE, ScenarioType.ANY));
        inOrder.verify(reporter).successful(lifecycleStepNameFor(Stage.BEFORE));
        inOrder.verify(reporter).successful("step <one>");
        inOrder.verify(reporter).successful("step <two>");
        inOrder.verify(reporter).successful(lifecycleStepNameFor(Stage.AFTER));
        inOrder.verify(reporter).successful(stepNameFor(Stage.AFTER, ScenarioType.ANY));
        inOrder.verify(reporter).successful(stepNameFor(Stage.AFTER, ScenarioType.EXAMPLE));

        inOrder.verify(reporter).successful(stepNameFor(Stage.AFTER, ScenarioType.NORMAL));

    }

    @Test
    public void shouldRunAfterAndBeforeScenarioSteps() throws Throwable{
        // Given
        Scenario scenario1 = new Scenario("my title 1", Meta.EMPTY, GivenStories.EMPTY, ExamplesTable.EMPTY, asList("step"));
        Story story = new Story(new Description("my blurb"), Narrative.EMPTY, asList(scenario1));
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        Configuration configuration = configurationWith(reporter, collector, failureStrategy);
        configuration.storyControls().doDryRun(true);
        CandidateSteps mySteps = new Steps(configuration);
        Step firstStep = mockSuccessfulStep(reporter, "step");
        when(collector.collectScenarioSteps(asList(mySteps), scenario1, new HashMap<String, String>())).thenReturn(
                asList(firstStep));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);

        givenBeforeAndAfterScenarioSteps(reporter, ScenarioType.NORMAL, collector, mySteps);
        givenBeforeAndAfterScenarioSteps(reporter, ScenarioType.ANY, collector, mySteps);

        givenLifecycleSteps(reporter, collector, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configuration, asList(mySteps), story);

        // Then
        InOrder inOrder = inOrder(reporter, failureStrategy);
        inOrder.verify(reporter).successful(stepNameFor(Stage.BEFORE, ScenarioType.NORMAL));
        inOrder.verify(reporter).successful(stepNameFor(Stage.BEFORE, ScenarioType.ANY));
        inOrder.verify(reporter).successful(lifecycleStepNameFor(Stage.BEFORE));
        inOrder.verify(reporter).successful("step");
        inOrder.verify(reporter).successful(lifecycleStepNameFor(Stage.AFTER));
        inOrder.verify(reporter).successful(stepNameFor(Stage.AFTER, ScenarioType.ANY));
        inOrder.verify(reporter).successful(stepNameFor(Stage.AFTER, ScenarioType.NORMAL));

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
        StoryReporter reporter = mock(StoryReporter.class);
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class, "result");
        when(step.perform(reporter, null)).thenReturn(result);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy failureStrategy = mock(FailureStrategy.class);
        Configuration configuration = configurationWith(reporter, collector, failureStrategy);
        configuration.doDryRun(true);
        CandidateSteps mySteps = new Steps(configuration);
        UUIDExceptionWrapper failure = new UUIDExceptionWrapper(new IllegalArgumentException());
        Step successfulStep = mockSuccessfulStep(reporter, "successfulStep");
        Step pendingStep = mock(Step.class, "pendingStep");
        Step failingStep = mock(Step.class, "failingStep");
        when(pendingStep.perform(eq(reporter), any())).thenReturn(pending("pendingStep"));
        when(pendingStep.doNotPerform(reporter, failure)).thenReturn(pending("pendingStep"));
        when(failingStep.perform(eq(reporter), any())).thenReturn(failed("failingStep", failure));
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
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy strategy = mock(FailureStrategy.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), any(), eq(parameters))).thenReturn(asList());
        Meta meta = new Meta(asList("some property"));
        Story story = new Story("", Description.EMPTY, meta, Narrative.EMPTY, asList(new Scenario()));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);
        String filterAsString = "-some property";
        MetaFilter filter = new MetaFilter(filterAsString);
        
        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector, strategy), asList(mySteps), story, filter);

        // Then
        verify(reporter).beforeStory(story, givenStory);
        verify(reporter).storyNotAllowed(story, filterAsString);
        verify(reporter).afterStory(givenStory);
    }
    
    @Test
    public void shouldNotRunStoriesNotAllowedByFilterOnStoryElement() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy strategy = mock(FailureStrategy.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), any(), eq(parameters))).thenReturn(asList());
        Story story = new Story("excluded_path", Description.EMPTY, Meta.EMPTY, Narrative.EMPTY, asList(new Scenario()));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);
        String filterAsString = "-story_path excluded_path";
        MetaFilter filter = new MetaFilter(filterAsString);
        
        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWith(reporter, collector, strategy);
        configuration.storyControls().useStoryMetaPrefix("story_");
        runner.run(configuration, asList(mySteps), story, filter);

        // Then
        verify(reporter).beforeStory(story, givenStory);
        verify(reporter).storyNotAllowed(story, filterAsString);
        verify(reporter).afterStory(givenStory);
    }
    
    @Test
    public void shouldNotRunScenariosNotAllowedByFilter() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy strategy = mock(FailureStrategy.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), any(), eq(parameters))).thenReturn(asList());
        Meta meta = new Meta(asList("some property"));
        Story story = new Story("", Description.EMPTY, Meta.EMPTY, Narrative.EMPTY, asList(new Scenario("", meta, GivenStories.EMPTY, ExamplesTable.EMPTY, asList(""))));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);
        String filterAsString = "-some property";
        MetaFilter filter = new MetaFilter(filterAsString);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, collector, strategy), asList(mySteps), story, filter);

        // Then
        verify(reporter).beforeStory(story, givenStory);
        verify(reporter).beforeScenario("");
        verify(reporter).scenarioNotAllowed(story.getScenarios().get(0), filterAsString);
        verify(reporter).afterScenario();
    }

    @Test
    public void shouldNotRunScenariosNotAllowedByFilterOnScenarioElement() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        StepCollector collector = mock(StepCollector.class);
        FailureStrategy strategy = mock(FailureStrategy.class);
        CandidateSteps mySteps = new Steps();
        when(collector.collectScenarioSteps(eq(asList(mySteps)), any(), eq(parameters))).thenReturn(asList());
        Story story = new Story("", Description.EMPTY, Meta.EMPTY, Narrative.EMPTY, asList(new Scenario("excluded_title", Meta.EMPTY, GivenStories.EMPTY, ExamplesTable.EMPTY, asList(""))));
        boolean givenStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, givenStory, collector, mySteps);
        String filterAsString = "-scenario_title excluded_title";
        MetaFilter filter = new MetaFilter(filterAsString);

        // When
        StoryRunner runner = new StoryRunner();
        Configuration configuration = configurationWith(reporter, collector, strategy);
        configuration.storyControls().useScenarioMetaPrefix("scenario_");
        runner.run(configuration, asList(mySteps), story, filter);

        // Then
        verify(reporter).beforeStory(story, givenStory);
        verify(reporter).beforeScenario("excluded_title");
        verify(reporter).scenarioNotAllowed(story.getScenarios().get(0), filterAsString);
        verify(reporter).afterScenario();
    }

    private void givenStoryWithNoBeforeOrAfterSteps(Story story, boolean givenStory, StepCollector collector, CandidateSteps mySteps) {
        List<Step> steps = asList();
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.BEFORE, givenStory)).thenReturn(steps);
        when(collector.collectBeforeOrAfterStorySteps(asList(mySteps), story, Stage.AFTER, givenStory)).thenReturn(steps);
    }

    private void givenLifecycleSteps(StoryReporter reporter, StepCollector collector, CandidateSteps mySteps) {
        Step beforeStep = mockSuccessfulStep(reporter, lifecycleStepNameFor(Stage.BEFORE));
        Step afterStep = mockSuccessfulStep(reporter, lifecycleStepNameFor(Stage.AFTER));
        Map<Stage, List<Step>> steps = new EnumMap<>(Stage.class);
        steps.put(Stage.BEFORE, Collections.singletonList(beforeStep));
        steps.put(Stage.AFTER, Collections.singletonList(afterStep));
        when(collector
                .collectLifecycleSteps(eq(asList(mySteps)), eq(Lifecycle.EMPTY), any(Meta.class), eq(Scope.SCENARIO)))
                .thenReturn(steps);
    }

    private String lifecycleStepNameFor(Stage stage) {
        return String.format("Lifecycle %s Step", stage);
    }

    private void givenBeforeAndAfterScenarioSteps(StoryReporter reporter, ScenarioType scenarioType,
            StepCollector collector, CandidateSteps mySteps) {
        givenBeforeOrAfterScenarioStep(reporter, Stage.BEFORE, scenarioType, collector, mySteps);
        givenBeforeOrAfterScenarioStep(reporter, Stage.AFTER, scenarioType, collector, mySteps);
    }

    private void givenBeforeOrAfterScenarioStep(StoryReporter reporter, Stage stage, ScenarioType scenarioType,
            StepCollector collector, CandidateSteps mySteps) {
        Step step = mockSuccessfulStep(reporter, stepNameFor(stage, scenarioType));
        when(collector.collectBeforeOrAfterScenarioSteps(eq(asList(mySteps)), any(), eq(stage), eq(scenarioType))).thenReturn(asList(step));
    }

    private String stepNameFor(Stage stage, ScenarioType scenarioType) {
        return String.format("%s %s Step", stage, scenarioType);
    }

    private Configuration configurationWithPendingStrategy(StepCollector collector, StoryReporter reporter,
                                                                PendingStepStrategy strategy) {
        LoadFromClasspath resourceLoadder = new LoadFromClasspath();
        RegexStoryParser storyParser = new RegexStoryParser(resourceLoadder, new TableTransformers());
        return configurationWith(storyParser, resourceLoadder, reporter, collector, new RethrowingFailure(), strategy);
    }

    private Configuration configurationWith(final StoryReporter reporter, final StepCollector collector) {
        return configurationWith(reporter, collector, new RethrowingFailure());
    }

    private Configuration configurationWith(StoryReporter reporter, StepCollector collector, FailureStrategy failureStrategy) {
        LoadFromClasspath resourceLoadder = new LoadFromClasspath();
        RegexStoryParser storyParser = new RegexStoryParser(resourceLoadder, new TableTransformers());
        return configurationWith(storyParser, resourceLoadder, reporter, collector, failureStrategy);
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

    private Step mockSuccessfulStep(StoryReporter reporter, String result) {
        Step step = mock(Step.class, result);        
        when(step.perform(eq(reporter), any())).thenReturn(successful(result));
        when(step.doNotPerform(eq(reporter), any())).thenReturn(notPerformed("successfulStep"));
        return step;
    }
    
}
