package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.AbstractStepResult.Ignorable;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepFinder.ByLevenshteinDistance;
import org.junit.Test;

public class MarkUnmatchedStepsAsPendingBehaviour {

    private Map<String, String> parameters = new HashMap<String, String>();

    @Test
    public void shouldCreateExecutableStepsWhenCandidatesAreMatched() {
        // Given
        StepCollector stepCollector = new MarkUnmatchedStepsAsPending();

        StepCandidate candidate = mock(StepCandidate.class);
        CandidateSteps steps = mock(Steps.class);
        Step executableStep = mock(Step.class);

        when(candidate.matches("my step")).thenReturn(true);
        when(candidate.createMatchedStep("my step", parameters)).thenReturn(executableStep);
        when(steps.listCandidates()).thenReturn(asList(candidate));

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(asList(steps), new Scenario(asList("my step")),
                parameters);

        // Then
        assertThat(executableSteps.size(), equalTo(1));
        assertThat(executableSteps.get(0), equalTo(executableStep));
    }

    @Test
    public void shouldCreateExecutableStepsOnlyFromPreviousNonAndStep() {
        // Given
        StepCollector stepCollector = new MarkUnmatchedStepsAsPending();

        CandidateSteps steps = mock(Steps.class);
        StepCandidate candidate = mock(StepCandidate.class, "candidate");
        StepCandidate andCandidate = mock(StepCandidate.class, "andCandidate");
        Step step = mock(Step.class);
        Step andStep = mock(Step.class);

        String myStep = "my step";
        when(candidate.matches(myStep)).thenReturn(true);
        when(candidate.createMatchedStep(myStep, parameters)).thenReturn(step);
        when(andCandidate.isAndStep(myStep)).thenReturn(false);
        String myAndStep = "my And step";
        when(andCandidate.matches(myAndStep)).thenReturn(true);
        when(andCandidate.isAndStep(myAndStep)).thenReturn(true);
        when(andCandidate.createMatchedStep(myAndStep, parameters)).thenReturn(andStep);

        when(steps.listCandidates()).thenReturn(asList(candidate, andCandidate));

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(asList(steps), new Scenario(asList(myStep, myAndStep)),
                parameters);

        // Then
        assertThat(executableSteps.size(), equalTo(2));
    }
    
    
    @Test
    public void shouldCreateExecutableStepsWhenACompositeStepIsMatched() {
        // Given
        StepCollector stepCollector = new MarkUnmatchedStepsAsPending();

        StepCandidate compositeCandidate = mock(StepCandidate.class, "compositeCandidate");
        StepCandidate composedCandidate1 = mock(StepCandidate.class, "composedCandidate1");
        StepCandidate composedCandidate2 = mock(StepCandidate.class, "composedCandidate2");
        CandidateSteps steps = mock(Steps.class);
        Step executableCompositeStep = mock(Step.class, "composite");
        Step executableComposedStep1 = mock(Step.class, "composed1");
        Step executableComposedStep2 = mock(Step.class, "composed2");

        List<StepCandidate> allCandidates = asList(compositeCandidate, composedCandidate1, composedCandidate2);
        when(steps.listCandidates()).thenReturn(allCandidates);
        String compositeStepAsString = "my composite step";
        when(compositeCandidate.matches(compositeStepAsString)).thenReturn(true);
        when(compositeCandidate.createMatchedStep(compositeStepAsString, parameters)).thenReturn(executableCompositeStep);
        when(compositeCandidate.isComposite()).thenReturn(true);
        when(compositeCandidate.createComposedSteps(compositeStepAsString, parameters, allCandidates)).thenReturn(asList(executableComposedStep1, executableComposedStep2));
        String composedStep1AsString = "my composed step 1";
        when(composedCandidate1.matches(composedStep1AsString)).thenReturn(true);
        when(composedCandidate1.createMatchedStep(composedStep1AsString, parameters)).thenReturn(executableComposedStep1);
        String composedStep2AsString = "my composed step 2";
        when(composedCandidate2.matches(composedStep2AsString)).thenReturn(true);
        when(composedCandidate2.createMatchedStep(composedStep1AsString, parameters)).thenReturn(executableComposedStep2);

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(asList(steps), new Scenario(asList(compositeStepAsString)),
                parameters);

        // Then
        assertThat(executableSteps.size(), equalTo(3));
        assertThat(executableSteps.get(0), equalTo(executableCompositeStep));
        assertThat(executableSteps.get(1), equalTo(executableComposedStep1));
        assertThat(executableSteps.get(2), equalTo(executableComposedStep2));
    }

    
    @Test
    public void shouldCreatePendingStepsWhenCandidatesAreNotMatched() {
        // Given
        StepCollector stepCollector = new MarkUnmatchedStepsAsPending();

        StepCandidate candidate = mock(StepCandidate.class);
        CandidateSteps steps = mock(Steps.class);

        String stepAsString = "my step";
        when(candidate.matches(stepAsString)).thenReturn(false);
        when(steps.listCandidates()).thenReturn(asList(candidate));

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(asList(steps), new Scenario(asList(stepAsString)),
                parameters);
        // Then
        assertThat(executableSteps.size(), equalTo(1));
        StepResult result = executableSteps.get(0).perform();
        Throwable throwable = result.getFailure();
        assertThat(throwable, is(PendingStepFound.class));
        assertThat(throwable.getMessage(), equalTo(stepAsString));
    }
    
    @Test
    public void shouldCreateIgnorableSteps() {
        // Given
        StepCollector stepCollector = new MarkUnmatchedStepsAsPending();

        StepCandidate candidate = mock(StepCandidate.class);
        CandidateSteps steps = mock(Steps.class);

        String stepAsString = "my ignorable step";
        when(candidate.ignore(stepAsString)).thenReturn(true);
        when(steps.listCandidates()).thenReturn(asList(candidate));

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(asList(steps), new Scenario(asList(stepAsString)),
                parameters);
        // Then
        assertThat(executableSteps.size(), equalTo(1));
        StepResult result = executableSteps.get(0).perform();
        assertThat(result, Matchers.instanceOf(Ignorable.class));
    }


    @Test
    public void shouldCollectBeforeAndAfterScenarioAnnotatedSteps() {
        // Given some candidate steps classes with before and after scenario
        // methods
        CandidateSteps steps1 = mock(Steps.class);
        CandidateSteps steps2 = mock(Steps.class);
        BeforeOrAfterStep bafStep11 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep12 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep21 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep22 = mock(BeforeOrAfterStep.class);
        Step stepBefore1 = mock(Step.class);
        Step stepBefore2 = mock(Step.class);
        Step stepAfter1 = mock(Step.class);
        Step stepAfter2 = mock(Step.class);
        when(bafStep11.getStage()).thenReturn(Stage.BEFORE);
        when(bafStep11.createStep()).thenReturn(stepBefore1);
        when(bafStep12.getStage()).thenReturn(Stage.BEFORE);
        when(bafStep12.createStep()).thenReturn(stepBefore2);
        when(bafStep21.getStage()).thenReturn(Stage.AFTER);
        when(bafStep21.createStepUponOutcome()).thenReturn(stepAfter1);
        when(bafStep22.getStage()).thenReturn(Stage.AFTER);
        when(bafStep22.createStepUponOutcome()).thenReturn(stepAfter2);
        when(steps1.listBeforeOrAfterScenario()).thenReturn(asList(bafStep11, bafStep12));
        when(steps2.listBeforeOrAfterScenario()).thenReturn(asList(bafStep21, bafStep22));

        // When we collect the list of steps
        StepCollector stepCollector = new MarkUnmatchedStepsAsPending();
        List<Step> beforeSteps = stepCollector.collectBeforeOrAfterScenarioSteps(asList(steps1, steps2), Stage.BEFORE);
        List<Step> afterSteps = stepCollector.collectBeforeOrAfterScenarioSteps(asList(steps1, steps2), Stage.AFTER);

        
        // Then all before and after steps should be added
        assertThat(beforeSteps, equalTo(asList(stepBefore1, stepBefore2)));
        assertThat(afterSteps, equalTo(asList(stepAfter1, stepAfter2)));
    }

    @Test
    public void shouldCollectBeforeAndAfterStoryAnnotatedSteps() {
        // Given some candidate steps classes with before and after story
        // methods
        CandidateSteps steps1 = mock(Steps.class);
        CandidateSteps steps2 = mock(Steps.class);
        BeforeOrAfterStep bafStep11 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep21 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep12 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep22 = mock(BeforeOrAfterStep.class);
        Step stepBefore1 = mock(Step.class);
        Step stepBefore2 = mock(Step.class);
        Step stepAfter1 = mock(Step.class);
        Step stepAfter2 = mock(Step.class);

        boolean givenStory = false;
        when(bafStep11.getStage()).thenReturn(Stage.BEFORE);
        when(bafStep11.createStep()).thenReturn(stepBefore1);
        when(bafStep21.getStage()).thenReturn(Stage.BEFORE);
        when(bafStep21.createStep()).thenReturn(stepBefore2);
        when(bafStep12.getStage()).thenReturn(Stage.AFTER);
        when(bafStep12.createStep()).thenReturn(stepAfter1);
        when(bafStep22.getStage()).thenReturn(Stage.AFTER);
        when(bafStep22.createStep()).thenReturn(stepAfter2);
        when(steps1.listBeforeOrAfterStory(givenStory)).thenReturn(asList(bafStep11, bafStep12));
        when(steps2.listBeforeOrAfterStory(givenStory)).thenReturn(asList(bafStep21, bafStep22));

        // When we collect the list of steps
        StepCollector stepCollector = new MarkUnmatchedStepsAsPending();
        List<Step> beforeSteps = stepCollector.collectBeforeOrAfterStorySteps(asList(steps1, steps2), new Story(),
                Stage.BEFORE, givenStory);
        List<Step> afterSteps = stepCollector.collectBeforeOrAfterStorySteps(asList(steps1, steps2), new Story(),
                Stage.AFTER, givenStory);

        // Then all before and after steps should be added
        assertThat(beforeSteps, equalTo(asList(stepBefore1, stepBefore2)));
        assertThat(afterSteps, equalTo(asList(stepAfter1, stepAfter2)));
    }

    @Test
    public void shouldCollectBeforeAndAfterStoriesAnnotatedSteps() {
        // Given some candidate steps classes with before and after stories
        // methods
        CandidateSteps steps1 = mock(Steps.class);
        CandidateSteps steps2 = mock(Steps.class);
        BeforeOrAfterStep bafStep11 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep21 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep12 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep22 = mock(BeforeOrAfterStep.class);
        Step stepBefore1 = mock(Step.class);
        Step stepBefore2 = mock(Step.class);
        Step stepAfter1 = mock(Step.class);
        Step stepAfter2 = mock(Step.class);

        when(bafStep11.getStage()).thenReturn(Stage.BEFORE);
        when(bafStep11.createStep()).thenReturn(stepBefore1);
        when(bafStep21.getStage()).thenReturn(Stage.BEFORE);
        when(bafStep21.createStep()).thenReturn(stepBefore2);
        when(bafStep12.getStage()).thenReturn(Stage.AFTER);
        when(bafStep12.createStep()).thenReturn(stepAfter1);
        when(bafStep22.getStage()).thenReturn(Stage.AFTER);
        when(bafStep22.createStep()).thenReturn(stepAfter2);
        when(steps1.listBeforeOrAfterStories()).thenReturn(asList(bafStep11, bafStep12));
        when(steps2.listBeforeOrAfterStories()).thenReturn(asList(bafStep21, bafStep22));

        // When we collect the list of steps
        StepCollector stepCollector = new MarkUnmatchedStepsAsPending();
        List<Step> beforeSteps = stepCollector.collectBeforeOrAfterStoriesSteps(asList(steps1, steps2), Stage.BEFORE);
        List<Step> afterSteps = stepCollector.collectBeforeOrAfterStoriesSteps(asList(steps1, steps2), Stage.AFTER);

        // Then all before and after steps should be added
        assertThat(beforeSteps, equalTo(asList(stepBefore1, stepBefore2)));
        assertThat(afterSteps, equalTo(asList(stepAfter1, stepAfter2)));
    }

    @Test
    public void shouldSortCandidateStepsByPriorityByDefault() {
        // Given some candidate steps classes
        // and some methods split across them
        CandidateSteps steps1 = mock(Steps.class);
        CandidateSteps steps2 = mock(Steps.class);
        StepCandidate candidate1 = mock(StepCandidate.class);
        StepCandidate candidate2 = mock(StepCandidate.class);
        StepCandidate candidate3 = mock(StepCandidate.class);
        StepCandidate candidate4 = mock(StepCandidate.class);
        Step step1 = mock(Step.class);
        Step step2 = mock(Step.class);
        Step step3 = mock(Step.class);
        Step step4 = mock(Step.class);

        when(steps1.listCandidates()).thenReturn(asList(candidate1, candidate2));
        when(steps2.listCandidates()).thenReturn(asList(candidate3, candidate4));

        // all matching the same step string with different priorities
        String stepAsString = "Given a step";
        when(candidate1.matches(stepAsString)).thenReturn(true);
        when(candidate2.matches(stepAsString)).thenReturn(true);
        when(candidate3.matches(stepAsString)).thenReturn(true);
        when(candidate4.matches(stepAsString)).thenReturn(true);
        when(candidate1.getPriority()).thenReturn(1);
        when(candidate2.getPriority()).thenReturn(2);
        when(candidate3.getPriority()).thenReturn(3);
        when(candidate4.getPriority()).thenReturn(4);
        when(candidate1.createMatchedStep(stepAsString, parameters)).thenReturn(step1);
        when(candidate2.createMatchedStep(stepAsString, parameters)).thenReturn(step2);
        when(candidate3.createMatchedStep(stepAsString, parameters)).thenReturn(step3);
        when(candidate4.createMatchedStep(stepAsString, parameters)).thenReturn(step4);

        // When we collect the list of steps
        StepCollector stepCollector = new MarkUnmatchedStepsAsPending();
        List<Step> steps = stepCollector.collectScenarioSteps(asList(steps1, steps2), new Scenario(asList(stepAsString)),
                parameters);

        // Then the step with highest priority is returned
        assertThat(step4, equalTo(steps.get(0)));
    }

    @Test
    public void shouldPrioritiseCandidateStepsByInjectableStrategy() {
        // Given some candidate steps classes
        // and some methods split across them
        CandidateSteps steps1 = mock(Steps.class);
        CandidateSteps steps2 = mock(Steps.class);
        StepCandidate candidate1 = mock(StepCandidate.class);
        StepCandidate candidate2 = mock(StepCandidate.class);
        StepCandidate candidate3 = mock(StepCandidate.class);
        StepCandidate candidate4 = mock(StepCandidate.class);
        Step step1 = mock(Step.class);
        Step step2 = mock(Step.class);
        Step step3 = mock(Step.class);
        Step step4 = mock(Step.class);

        when(steps1.listCandidates()).thenReturn(asList(candidate1, candidate2));
        when(steps2.listCandidates()).thenReturn(asList(candidate3, candidate4));

        // all matching the same step string with different priorities
        String stepAsString = "Given a step";
        when(candidate1.matches(stepAsString)).thenReturn(true);
        when(candidate2.matches(stepAsString)).thenReturn(true);
        when(candidate3.matches(stepAsString)).thenReturn(true);
        when(candidate4.matches(stepAsString)).thenReturn(true);
        when(candidate1.getPatternAsString()).thenReturn("Given I do something");
        when(candidate2.getPatternAsString()).thenReturn("When I do something ");
        when(candidate3.getPatternAsString()).thenReturn("Then I do something");
        when(candidate4.getPatternAsString()).thenReturn("And I do something");
        when(candidate1.createMatchedStep(stepAsString, parameters)).thenReturn(step1);
        when(candidate2.createMatchedStep(stepAsString, parameters)).thenReturn(step2);
        when(candidate3.createMatchedStep(stepAsString, parameters)).thenReturn(step3);
        when(candidate4.createMatchedStep(stepAsString, parameters)).thenReturn(step4);

        StepCollector stepCollector = new MarkUnmatchedStepsAsPending(new StepFinder(new ByLevenshteinDistance()));
        List<Step> steps = stepCollector.collectScenarioSteps(asList(steps1, steps2), new Scenario(asList(stepAsString)),
                parameters);

        // Then the step with highest priority is returned
        assertThat(step4, equalTo(steps.get(0)));
    }

}
