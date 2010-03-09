package org.jbehave.scenario;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.scenario.definition.Blurb;
import org.jbehave.scenario.definition.ScenarioDefinition;
import org.jbehave.scenario.definition.StoryDefinition;
import org.jbehave.scenario.errors.ErrorStrategy;
import org.jbehave.scenario.errors.ErrorStrategyInWhichWeTrustTheReporter;
import org.jbehave.scenario.errors.PendingErrorStrategy;
import org.jbehave.scenario.parser.ClasspathScenarioDefiner;
import org.jbehave.scenario.parser.ScenarioDefiner;
import org.jbehave.scenario.reporters.ScenarioReporter;
import org.jbehave.scenario.steps.CandidateStep;
import org.jbehave.scenario.steps.CandidateSteps;
import org.jbehave.scenario.steps.Step;
import org.jbehave.scenario.steps.StepCreator;
import org.jbehave.scenario.steps.StepResult;
import org.jbehave.scenario.steps.Steps;
import org.jbehave.scenario.steps.StepCreator.Stage;
import org.junit.Test;
import org.mockito.InOrder;

public class ScenarioRunnerBehaviour {

    private Map<String, String> tableRow = new HashMap<String, String>();

    @Test
    public void shouldRunStepsInScenariosAndReportResultsToReporter() throws Throwable {
        // Given
        ScenarioDefinition scenarioDefinition1 = new ScenarioDefinition("my title 1", asList("failingStep",
                "successfulStep"));
        ScenarioDefinition scenarioDefinition2 = new ScenarioDefinition("my title 2", asList("successfulStep"));
        ScenarioDefinition scenarioDefinition3 = new ScenarioDefinition("my title 3", asList("successfulStep",
                "pendingStep"));
        StoryDefinition storyDefinition = new StoryDefinition(new Blurb("my blurb"), scenarioDefinition1,
                scenarioDefinition2, scenarioDefinition3);
        boolean embeddedStory = false;

        CandidateStep[] someCandidateSteps = new CandidateStep[0];
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform()).thenReturn(result);
        ScenarioReporter reporter = mock(ScenarioReporter.class);
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mock(Steps.class);
        when(mySteps.getSteps()).thenReturn(someCandidateSteps);
        IllegalArgumentException anException = new IllegalArgumentException();
        Step pendingStep = mock(Step.class);
        Step successfulStep = mock(Step.class);
        Step failingStep = mock(Step.class);
        when(pendingStep.perform()).thenReturn(StepResult.pending("pendingStep"));
        when(successfulStep.perform()).thenReturn(StepResult.success("successfulStep"));
        when(successfulStep.doNotPerform()).thenReturn(StepResult.notPerformed("successfulStep"));
        when(failingStep.perform()).thenReturn(StepResult.failure("failingStep", anException));
        when(creator.createStepsFrom(scenarioDefinition1, tableRow, mySteps)).thenReturn(
                new Step[] { failingStep, successfulStep });
        when(creator.createStepsFrom(scenarioDefinition2, tableRow, mySteps)).thenReturn(new Step[] { successfulStep });
        when(creator.createStepsFrom(scenarioDefinition3, tableRow, mySteps)).thenReturn(
                new Step[] { successfulStep, pendingStep });
        givenStoryWithNoBeforeOrAfterSteps(storyDefinition, embeddedStory, creator, mySteps);

        // When
        ErrorStrategy errorStrategy = mock(ErrorStrategy.class);
        ScenarioRunner runner = new ScenarioRunner();
        runner.run(storyDefinition, configurationWith(reporter, creator, errorStrategy), embeddedStory, mySteps);

        // Then
        InOrder inOrder = inOrder(reporter, errorStrategy);
        inOrder.verify(reporter).beforeStory(storyDefinition, embeddedStory);
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
    public void shouldRunGivenScenariosBeforeSteps() throws Throwable {
        // Given
        ScenarioDefinition scenarioDefinition1 = new ScenarioDefinition("scenario 1", asList("successfulStep"));
        List<String> givenScenarios = asList("/path/to/given/scenario1");
        ScenarioDefinition scenarioDefinition2 = new ScenarioDefinition("scenario 2", givenScenarios,
                asList("anotherSuccessfulStep"));
        StoryDefinition storyDefinition1 = new StoryDefinition(new Blurb("story 1"), scenarioDefinition1);
        StoryDefinition storyDefinition2 = new StoryDefinition(new Blurb("story 2"), scenarioDefinition2);
        boolean embeddedStory = false;

        CandidateStep[] someCandidateSteps = new CandidateStep[0];
        Step step = mock(Step.class);
        StepResult result = mock(StepResult.class);
        when(step.perform()).thenReturn(result);

        ScenarioDefiner scenarioDefiner = mock(ScenarioDefiner.class);
        ScenarioReporter reporter = mock(ScenarioReporter.class);
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mock(Steps.class);
        when(mySteps.getSteps()).thenReturn(someCandidateSteps);
        Step successfulStep = mock(Step.class);
        when(successfulStep.perform()).thenReturn(StepResult.success("successfulStep"));
        Step anotherSuccessfulStep = mock(Step.class);
        when(anotherSuccessfulStep.perform()).thenReturn(StepResult.success("anotherSuccessfulStep"));        
        givenStoryWithNoBeforeOrAfterSteps(storyDefinition1, embeddedStory, creator, mySteps);
        when(creator.createStepsFrom(scenarioDefinition1, tableRow, mySteps)).thenReturn(new Step[] { successfulStep });
        givenStoryWithNoBeforeOrAfterSteps(storyDefinition2, embeddedStory, creator, mySteps);
        when(creator.createStepsFrom(scenarioDefinition2, tableRow, mySteps)).thenReturn(
                new Step[] { anotherSuccessfulStep });
        when(scenarioDefiner.loadScenarioDefinitionsFor("/path/to/given/scenario1")).thenReturn(storyDefinition1);
        givenStoryWithNoBeforeOrAfterSteps(storyDefinition1, embeddedStory, creator, mySteps);
        givenStoryWithNoBeforeOrAfterSteps(storyDefinition2, embeddedStory, creator, mySteps);
        ErrorStrategy errorStrategy = mock(ErrorStrategy.class);

        // When
        ScenarioRunner runner = new ScenarioRunner();
        runner.run(storyDefinition2, configurationWith(scenarioDefiner, reporter, creator, errorStrategy), embeddedStory,
                mySteps);

        // Then
        InOrder inOrder = inOrder(reporter);
        inOrder.verify(reporter).beforeStory(storyDefinition2, embeddedStory);
        inOrder.verify(reporter).givenScenarios(givenScenarios);
        inOrder.verify(reporter).successful("successfulStep");
        inOrder.verify(reporter).successful("anotherSuccessfulStep");
        inOrder.verify(reporter).afterStory(embeddedStory);
        verify(reporter, never()).beforeStory(storyDefinition1, embeddedStory);
    }

    @Test
    public void shouldNotPerformStepsAfterStepsWhichShouldNotContinue() throws Throwable {
        // Given
        ScenarioReporter reporter = mock(ScenarioReporter.class);
        Step firstStepNormal = mock(Step.class);
        Step secondStepPending = mock(Step.class);
        Step thirdStepNormal = mock(Step.class);
        Step fourthStepAlsoPending = mock(Step.class);
        StepCreator creator = mock(StepCreator.class);
        Steps mySteps = mock(Steps.class);
        when(creator.createStepsFrom((ScenarioDefinition) anyObject(), eq(tableRow), eq(mySteps))).thenReturn(
                new Step[] { firstStepNormal, secondStepPending, thirdStepNormal, fourthStepAlsoPending });
        when(firstStepNormal.perform()).thenReturn(StepResult.success("Given I succeed"));
        when(secondStepPending.perform()).thenReturn(StepResult.pending("When I am pending"));
        when(thirdStepNormal.doNotPerform()).thenReturn(StepResult.notPerformed("Then I should not be performed"));
        when(fourthStepAlsoPending.doNotPerform()).thenReturn(
                StepResult.notPerformed("Then I should not be performed either"));
        StoryDefinition storyDefinition = new StoryDefinition(new ScenarioDefinition(""));
        boolean embeddedStory = false;
        givenStoryWithNoBeforeOrAfterSteps(storyDefinition, embeddedStory, creator, mySteps);

        // When
        ScenarioRunner runner = new ScenarioRunner();
        runner.run(storyDefinition, configurationWith(reporter, creator), embeddedStory,
                mySteps);

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
        ScenarioReporter reporter = mock(ScenarioReporter.class);
        Step firstStepExceptional = mock(Step.class);
        Step secondStepNotPerformed = mock(Step.class);
        StepResult failure = StepResult.failure("When I fail", new IllegalStateException());
        StepResult notPerformed = StepResult.notPerformed("Then I should not be performed");
        when(firstStepExceptional.perform()).thenReturn(failure);
        when(secondStepNotPerformed.doNotPerform()).thenReturn(notPerformed);
        ErrorStrategy errorStrategy = mock(ErrorStrategy.class);
        StepCreator creator = mock(StepCreator.class);
        Steps mySteps = mock(Steps.class);
        when(creator.createStepsFrom((ScenarioDefinition) anyObject(), eq(tableRow), eq(mySteps))).thenReturn(
                new Step[] { firstStepExceptional, secondStepNotPerformed });
        StoryDefinition storyDefinition = new StoryDefinition(new ScenarioDefinition(""));
        boolean embeddedStory = false;
        givenStoryWithNoBeforeOrAfterSteps(storyDefinition, embeddedStory, creator, mySteps);

        // When
        ScenarioRunner runner = new ScenarioRunner();
        runner.run(storyDefinition, configurationWith(reporter, creator, errorStrategy), false, mySteps);

        // Then
        verify(firstStepExceptional).perform();
        verify(secondStepNotPerformed).doNotPerform();

        InOrder inOrder = inOrder(reporter, errorStrategy);
        inOrder.verify(reporter).beforeStory((StoryDefinition) anyObject(), eq(embeddedStory));
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
        ScenarioReporter reporter = mock(ScenarioReporter.class);
        Step pendingStep = mock(Step.class);
        Step secondStep = mock(Step.class);
        when(pendingStep.perform()).thenReturn(StepResult.pending("pendingStep"));
        when(secondStep.perform()).thenReturn(StepResult.success("secondStep"));
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mock(Steps.class);
        ScenarioDefinition scenario1 = mock(ScenarioDefinition.class);
        ScenarioDefinition scenario2 = mock(ScenarioDefinition.class);
        when(creator.createStepsFrom(scenario1, tableRow, mySteps)).thenReturn(new Step[] { pendingStep });
        when(creator.createStepsFrom(scenario2, tableRow, mySteps)).thenReturn(new Step[] { secondStep });
        StoryDefinition storyDefinition = new StoryDefinition(scenario1, scenario2);
        boolean embeddedStory = false;
        givenStoryWithNoBeforeOrAfterSteps(storyDefinition, embeddedStory, creator, mySteps);


        // When
        ScenarioRunner runner = new ScenarioRunner();
        runner.run(storyDefinition, configurationWith(reporter, creator), embeddedStory, mySteps);

        // Then
        verify(pendingStep).perform();
        verify(secondStep).perform();
        verify(secondStep, never()).doNotPerform();
    }

    @Test
    public void shouldRunBeforeAndAfterStorySteps() throws Throwable {
        // Given
        ScenarioReporter reporter = mock(ScenarioReporter.class);
        Step beforeStep = mock(Step.class);
        Step afterStep = mock(Step.class);
        when(beforeStep.perform()).thenReturn(StepResult.success("beforeStep"));
        when(afterStep.perform()).thenReturn(StepResult.success("secondStep"));
        StepCreator creator = mock(StepCreator.class);
        CandidateSteps mySteps = mock(Steps.class);
        StoryDefinition storyDefinition = new StoryDefinition();
        boolean embeddedStory = false;
        when(creator.createStepsFrom(storyDefinition, Stage.BEFORE, embeddedStory, mySteps)).thenReturn(new Step[] { beforeStep });
        when(creator.createStepsFrom(storyDefinition, Stage.AFTER, embeddedStory, mySteps)).thenReturn(new Step[] { afterStep });

        // When
        ScenarioRunner runner = new ScenarioRunner();
        runner.run(storyDefinition, configurationWith(reporter, creator), embeddedStory, mySteps);

        // Then
        verify(beforeStep).perform();
        verify(afterStep).perform();
    }
    
    
    @Test
    public void shouldHandlePendingStepsAccordingToStrategy() throws Throwable {
        // Given
        ScenarioReporter reporter = mock(ScenarioReporter.class);
        Step pendingStep = mock(Step.class);
        StepResult pendingResult = StepResult.pending("My step isn't defined!");
        when(pendingStep.perform()).thenReturn(pendingResult);
        PendingErrorStrategy strategy = mock(PendingErrorStrategy.class);
        StepCreator creator = mock(StepCreator.class);
        Steps mySteps = mock(Steps.class);
        when(creator.createStepsFrom((ScenarioDefinition) anyObject(), eq(tableRow), eq(mySteps))).thenReturn(
                new Step[] { pendingStep });
        StoryDefinition storyDefinition = new StoryDefinition(new ScenarioDefinition(""));
        boolean embeddedStory = false;
        givenStoryWithNoBeforeOrAfterSteps(storyDefinition, embeddedStory, creator, mySteps);


        // When
        ScenarioRunner runner = new ScenarioRunner();
        runner.run(storyDefinition, configurationWithPendingStrategy(creator, reporter,
                strategy), embeddedStory, mySteps);

        // Then
        verify(strategy).handleError(pendingResult.getThrowable());
    }

    private void givenStoryWithNoBeforeOrAfterSteps(StoryDefinition storyDefinition, boolean embeddedStory, StepCreator creator, CandidateSteps mySteps) {
        Step[] steps = new Step[] {};   
        when(creator.createStepsFrom(storyDefinition, Stage.BEFORE, embeddedStory, mySteps)).thenReturn(steps);
        when(creator.createStepsFrom(storyDefinition, Stage.AFTER, embeddedStory, mySteps)).thenReturn(steps);
    }

    private Configuration configurationWithPendingStrategy(StepCreator creator, ScenarioReporter reporter,
            PendingErrorStrategy strategy) {
        return configurationWith(new ClasspathScenarioDefiner(), reporter, creator,
                new ErrorStrategyInWhichWeTrustTheReporter(), strategy);
    }

    private Configuration configurationWith(final ScenarioReporter reporter, final StepCreator creator) {
        return configurationWith(reporter, creator, new ErrorStrategyInWhichWeTrustTheReporter());
    }

    private Configuration configurationWith(ScenarioReporter reporter, StepCreator creator, ErrorStrategy errorStrategy) {
        return configurationWith(new ClasspathScenarioDefiner(), reporter, creator, errorStrategy);
    }

    private Configuration configurationWith(ScenarioDefiner definer, final ScenarioReporter reporter,
            final StepCreator creator, final ErrorStrategy errorStrategy) {
        return configurationWith(definer, reporter, creator, errorStrategy, PendingErrorStrategy.PASSING);
    }

    private Configuration configurationWith(final ScenarioDefiner definer, final ScenarioReporter reporter,
            final StepCreator creator, final ErrorStrategy errorStrategy, final PendingErrorStrategy pendingStrategy) {

        return new PropertyBasedConfiguration() {
            @Override
            public ScenarioDefiner forDefiningScenarios() {
                return definer;
            }

            @Override
            public StepCreator forCreatingSteps() {
                return creator;
            }

            @Override
            public ScenarioReporter forReportingScenarios() {
                return reporter;
            }

            @Override
            public ErrorStrategy forHandlingErrors() {
                return errorStrategy;
            }

            @Override
            public PendingErrorStrategy forPendingSteps() {
                return pendingStrategy;
            }
        };
    }

}
