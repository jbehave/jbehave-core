package org.jbehave.core.runner;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.errors.ErrorStrategy;
import org.jbehave.core.errors.ErrorStrategyInWhichWeTrustTheReporter;
import org.jbehave.core.errors.PendingErrorStrategy;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.runner.StoryRunner;
import org.jbehave.core.steps.CandidateStep;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.MostUsefulStepsConfiguration;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepResult;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.StepCreator.Stage;
import org.junit.Test;
import org.mockito.InOrder;

public class StoryRunnerBehaviour {

    private Map<String, String> tableRow = new HashMap<String, String>();

    @Test
    public void shouldRunStepsInStoryAndReportResultsToReporter() throws Throwable {
        // Given
        Scenario scenario1 = new Scenario("my title 1", asList("failingStep",
                "successfulStep"));
        Scenario scenario2 = new Scenario("my title 2", asList("successfulStep"));
        Scenario scenario3 = new Scenario("my title 3", asList("successfulStep",
                "pendingStep"));
        Story story = new Story(new Description("my blurb"), scenario1,
                scenario2, scenario3);
        boolean embeddedStory = false;

        CandidateStep[] someCandidateSteps = new CandidateStep[0];
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform()).thenReturn(result);
        StoryReporter reporter = mock(StoryReporter.class);
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mock(Steps.class);
        when(mySteps.getConfiguration()).thenReturn(new MostUsefulStepsConfiguration());
        when(mySteps.getSteps()).thenReturn(someCandidateSteps);
        IllegalArgumentException anException = new IllegalArgumentException();
        Step pendingStep = mock(Step.class);
        Step successfulStep = mock(Step.class);
        Step failingStep = mock(Step.class);
        when(pendingStep.perform()).thenReturn(StepResult.pending("pendingStep"));
        when(successfulStep.perform()).thenReturn(StepResult.successful("successfulStep"));
        when(successfulStep.doNotPerform()).thenReturn(StepResult.notPerformed("successfulStep"));
        when(failingStep.perform()).thenReturn(StepResult.failed("failingStep", anException));
        when(creator.createStepsFrom(asList(mySteps), scenario1, tableRow)).thenReturn(
                asList(failingStep, successfulStep));
        when(creator.createStepsFrom(asList(mySteps), scenario2, tableRow)).thenReturn(asList(successfulStep));
        when(creator.createStepsFrom(asList(mySteps), scenario3, tableRow)).thenReturn(
                asList(successfulStep, pendingStep));
        givenStoryWithNoBeforeOrAfterSteps(story, embeddedStory, creator, mySteps);

        // When
        ErrorStrategy errorStrategy = mock(ErrorStrategy.class);
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, creator, errorStrategy), asList(mySteps), story, embeddedStory);

        // Then
        InOrder inOrder = inOrder(reporter, errorStrategy);
        inOrder.verify(reporter).beforeStory(story, embeddedStory);
        inOrder.verify(reporter).beforeScenario("my title 1");
        inOrder.verify(reporter).failed("failingStep", anException);
        inOrder.verify(reporter).notPerformed("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 2");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).beforeScenario("my title 3");
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).pending("pendingStep");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(embeddedStory);
        inOrder.verify(errorStrategy).handleError(anException);
    }

    @Test
    public void shouldRunGivenStoriesBeforeSteps() throws Throwable {
        // Given
        Scenario scenario1 = new Scenario("core 1", asList("successfulStep"));
        List<String> givenStories = asList("/path/to/given/story1");
        Scenario scenario2 = new Scenario("core 2", givenStories,
                asList("anotherSuccessfulStep"));
        Story story1 = new Story(new Description("story 1"), scenario1);
        Story story2 = new Story(new Description("story 2"), scenario2);
        boolean embeddedStory = false;

        CandidateStep[] someCandidateSteps = new CandidateStep[0];
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform()).thenReturn(result);

        StoryParser storyParser = mock(StoryParser.class);
        StoryLoader storyLoader = mock(StoryLoader.class);
        StoryReporter reporter = mock(StoryReporter.class);
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mock(Steps.class);
        when(mySteps.getConfiguration()).thenReturn(new MostUsefulStepsConfiguration());
        when(mySteps.getSteps()).thenReturn(someCandidateSteps);
        Step successfulStep = mock(Step.class);
        when(successfulStep.perform()).thenReturn(StepResult.successful("successfulStep"));
        Step anotherSuccessfulStep = mock(Step.class);
        when(anotherSuccessfulStep.perform()).thenReturn(StepResult.successful("anotherSuccessfulStep"));
        givenStoryWithNoBeforeOrAfterSteps(story1, embeddedStory, creator, mySteps);
        when(creator.createStepsFrom(asList(mySteps), scenario1, tableRow)).thenReturn(asList(successfulStep));
        givenStoryWithNoBeforeOrAfterSteps(story2, embeddedStory, creator, mySteps);
        when(creator.createStepsFrom(asList(mySteps), scenario2, tableRow)).thenReturn(
                asList(anotherSuccessfulStep));
        when(storyLoader.loadStoryAsText("/path/to/given/story1")).thenReturn("storyContent");
        when(storyParser.parseStory("storyContent", "/path/to/given/story1")).thenReturn(story1);
        givenStoryWithNoBeforeOrAfterSteps(story1, embeddedStory, creator, mySteps);
        givenStoryWithNoBeforeOrAfterSteps(story2, embeddedStory, creator, mySteps);
        ErrorStrategy errorStrategy = mock(ErrorStrategy.class);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(storyParser, storyLoader, reporter, creator, errorStrategy), asList(mySteps),
                 story2, embeddedStory);

        // Then
        InOrder inOrder = inOrder(reporter);
        inOrder.verify(reporter).beforeStory(story2, embeddedStory);
        inOrder.verify(reporter).givenStories(givenStories);
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).successful("anotherSuccessfulStep");
        inOrder.verify(reporter).afterStory(embeddedStory);
        verify(reporter, never()).beforeStory(story1, embeddedStory);
    }

    @Test
    public void shouldNotPerformStepsAfterStepsWhichShouldNotContinue() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step firstStepNormal = mock(Step.class);
        Step secondStepPending = mock(Step.class);
        Step thirdStepNormal = mock(Step.class);
        Step fourthStepAlsoPending = mock(Step.class);
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mockMySteps();
        when(creator.createStepsFrom(eq(asList(mySteps)), (Scenario) anyObject(), eq(tableRow))).thenReturn(
                asList(firstStepNormal, secondStepPending, thirdStepNormal, fourthStepAlsoPending));
        when(firstStepNormal.perform()).thenReturn(StepResult.successful("Given I succeed"));
        when(secondStepPending.perform()).thenReturn(StepResult.pending("When I am pending"));
        when(thirdStepNormal.doNotPerform()).thenReturn(StepResult.notPerformed("Then I should not be performed"));
        when(fourthStepAlsoPending.doNotPerform()).thenReturn(
                StepResult.notPerformed("Then I should not be performed either"));
        Story story = new Story(new Scenario(""));
        boolean embeddedStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, embeddedStory, creator, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, creator), asList(mySteps), story, embeddedStory);

        // Then
        verify(firstStepNormal).perform();
        verify(secondStepPending).perform();
        verify(thirdStepNormal).doNotPerform();
        verify(fourthStepAlsoPending).doNotPerform();

        verify(reporter).successful("Given I succeed");
        verify(reporter).pending("When I am pending");
        verify(reporter).notPerformed("Then I should not be performed");
        verify(reporter).notPerformed("Then I should not be performed either");
    }

    @Test
    public void shouldReportAnyThrowablesThenHandleAfterStoryIsFinished() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step firstStepExceptional = mock(Step.class);
        Step secondStepNotPerformed = mock(Step.class);
        StepResult failure = StepResult.failed("When I fail", new IllegalStateException());
        StepResult notPerformed = StepResult.notPerformed("Then I should not be performed");
        when(firstStepExceptional.perform()).thenReturn(failure);
        when(secondStepNotPerformed.doNotPerform()).thenReturn(notPerformed);
        ErrorStrategy errorStrategy = mock(ErrorStrategy.class);
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mockMySteps();
        when(creator.createStepsFrom(eq(asList(mySteps)), (Scenario) anyObject(), eq(tableRow))).thenReturn(
                asList(firstStepExceptional, secondStepNotPerformed));
        Story story = new Story(new Scenario(""));
        boolean embeddedStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, embeddedStory, creator, mySteps);

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, creator, errorStrategy), asList(mySteps), story, false);

        // Then
        verify(firstStepExceptional).perform();
        verify(secondStepNotPerformed).doNotPerform();

        InOrder inOrder = inOrder(reporter, errorStrategy);
        inOrder.verify(reporter).beforeStory((Story) anyObject(), eq(embeddedStory));
        inOrder.verify(reporter).beforeScenario((String) anyObject());
        inOrder.verify(reporter).failed("When I fail", failure.getThrowable());
        inOrder.verify(reporter).notPerformed("Then I should not be performed");
        inOrder.verify(reporter).afterScenario();
        inOrder.verify(reporter).afterStory(embeddedStory);
        inOrder.verify(errorStrategy).handleError(failure.getThrowable());
    }

    @Test
    public void shouldResetStateForEachSetOfSteps() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step pendingStep = mock(Step.class);
        Step secondStep = mock(Step.class);
        when(pendingStep.perform()).thenReturn(StepResult.pending("pendingStep"));
        when(secondStep.perform()).thenReturn(StepResult.successful("secondStep"));
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mockMySteps();        
        Scenario scenario1 = new Scenario("scenario1");
        Scenario scenario2 = new Scenario("scenario2");
        when(creator.createStepsFrom(asList(mySteps), scenario1, tableRow)).thenReturn(asList(pendingStep));
        when(creator.createStepsFrom(asList(mySteps), scenario2, tableRow)).thenReturn(asList(secondStep));
        Story story = new Story(scenario1, scenario2);
        boolean embeddedStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, embeddedStory, creator, mySteps);


        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, creator), asList(mySteps), story, embeddedStory);

        // Then
        verify(pendingStep).perform();
        verify(secondStep).perform();
        verify(secondStep, never()).doNotPerform();
    }

    @Test
    public void shouldRunBeforeAndAfterStorySteps() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step beforeStep = mock(Step.class);
        Step afterStep = mock(Step.class);
        when(beforeStep.perform()).thenReturn(StepResult.successful("beforeStep"));
        when(afterStep.perform()).thenReturn(StepResult.successful("secondStep"));
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mockMySteps();
        Story story = new Story();
        boolean embeddedStory = false;
        when(creator.createStepsFrom(asList(mySteps), story, Stage.BEFORE, embeddedStory)).thenReturn(asList(beforeStep));
        when(creator.createStepsFrom(asList(mySteps), story, Stage.AFTER, embeddedStory)).thenReturn(asList(afterStep));

        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWith(reporter, creator),asList(mySteps), story, embeddedStory);

        // Then
        verify(beforeStep).perform();
        verify(afterStep).perform();
    }


    @Test
    public void shouldHandlePendingStepsAccordingToStrategy() throws Throwable {
        // Given
        StoryReporter reporter = mock(StoryReporter.class);
        Step pendingStep = mock(Step.class);
        StepResult pendingResult = StepResult.pending("My step isn't defined!");
        when(pendingStep.perform()).thenReturn(pendingResult);
        PendingErrorStrategy strategy = mock(PendingErrorStrategy.class);
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mockMySteps();
        when(creator.createStepsFrom(eq(asList(mySteps)), (Scenario) anyObject(), eq(tableRow))).thenReturn(
                asList(pendingStep));
        Story story = new Story(new Scenario(""));
        boolean embeddedStory = false;
        givenStoryWithNoBeforeOrAfterSteps(story, embeddedStory, creator, mySteps);


        // When
        StoryRunner runner = new StoryRunner();
        runner.run(configurationWithPendingStrategy(creator, reporter,
                strategy), asList(mySteps), story, embeddedStory);

        // Then
        verify(strategy).handleError(pendingResult.getThrowable());
    }

	private CandidateSteps mockMySteps() {
		CandidateSteps mySteps = mock(CandidateSteps.class);
        when(mySteps.getConfiguration()).thenReturn(new MostUsefulStepsConfiguration());
		return mySteps;
	}

    private void givenStoryWithNoBeforeOrAfterSteps(Story story, boolean embeddedStory, StepCreator creator, CandidateSteps mySteps) {
        List<Step> steps = asList();
        when(creator.createStepsFrom(asList(mySteps), story, Stage.BEFORE, embeddedStory)).thenReturn(steps);
        when(creator.createStepsFrom(asList(mySteps), story, Stage.AFTER, embeddedStory)).thenReturn(steps);
    }

    private StoryConfiguration configurationWithPendingStrategy(StepCreator creator, StoryReporter reporter,
                                                                PendingErrorStrategy strategy) {
        return configurationWith(new RegexStoryParser(), new LoadFromClasspath(), reporter, creator,
                new ErrorStrategyInWhichWeTrustTheReporter(), strategy);
    }

    private StoryConfiguration configurationWith(final StoryReporter reporter, final StepCreator creator) {
        return configurationWith(reporter, creator, new ErrorStrategyInWhichWeTrustTheReporter());
    }

    private StoryConfiguration configurationWith(StoryReporter reporter, StepCreator creator, ErrorStrategy errorStrategy) {
        return configurationWith(new RegexStoryParser(), new LoadFromClasspath(), reporter, creator, errorStrategy);
    }

    private StoryConfiguration configurationWith(StoryParser parser, final StoryLoader storyLoader, final StoryReporter reporter,
                                                 final StepCreator creator, final ErrorStrategy errorStrategy) {
        return configurationWith(parser, storyLoader, reporter, creator, errorStrategy, PendingErrorStrategy.PASSING);
    }

    private StoryConfiguration configurationWith(final StoryParser parser, final StoryLoader loader, final StoryReporter reporter,
                                                 final StepCreator creator, final ErrorStrategy errorStrategy, final PendingErrorStrategy pendingStrategy) {

        return new MostUsefulStoryConfiguration() {
            @Override
            public StoryParser storyParser() {
                return parser;
            }

            @Override
            public StoryLoader storyLoader() {
                return loader;
            }

            @Override
            public StepCreator stepCreator() {
                return creator;
            }

            @Override
            public StoryReporter storyReporter() {
                return reporter;
            }

            @Override
            public ErrorStrategy errorStrategy() {
                return errorStrategy;
            }

            @Override
            public PendingErrorStrategy pendingErrorStrategy() {
                return pendingStrategy;
            }
        };
    }

}
