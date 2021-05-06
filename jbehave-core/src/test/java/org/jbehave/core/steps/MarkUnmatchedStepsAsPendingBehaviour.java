package org.jbehave.core.steps;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.embedder.MatchingStepMonitor;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.AbstractStepResult.Comment;
import org.jbehave.core.steps.AbstractStepResult.Ignorable;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.StepFinder.ByLevenshteinDistance;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarkUnmatchedStepsAsPendingBehaviour
{

    private final StepCollector stepCollector = new MarkUnmatchedStepsAsPending();
    private final StepMonitor stepMonitor = new NullStepMonitor();
    private final Map<String, String> parameters = new HashMap<>();

    @Test
    void shouldCreateExecutableStepsWhenCandidatesAreMatched()
    {
        // Given
        StepCandidate candidate = mock(StepCandidate.class);
        Step executableStep = mock(Step.class);

        String stepAsString = "my step";
        when(candidate.matches(stepAsString, null)).thenReturn(true);
        when(candidate.createMatchedStep(stepAsString, parameters, emptyList())).thenReturn(executableStep);

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(singletonList(candidate),
                createScenario(stepAsString), parameters, stepMonitor);

        // Then
        assertThat(executableSteps.size(), equalTo(1));
        assertThat(executableSteps.get(0), equalTo(executableStep));
    }

    @Test
    void shouldCreateExecutableStepsOnlyFromPreviousNonAndStep()
    {
        // Given
        StepCandidate candidate = mock(StepCandidate.class, "candidate");
        StepCandidate andCandidate = mock(StepCandidate.class, "andCandidate");
        Step step = mock(Step.class);
        Step andStep = mock(Step.class);

        String myStep = "my step";
        when(candidate.matches(myStep)).thenReturn(true);
        when(candidate.createMatchedStep(myStep, parameters, emptyList())).thenReturn(step);
        String myAndStep = "And my step";
        when(andCandidate.matches(myAndStep)).thenReturn(true);
        when(andCandidate.createMatchedStep(myAndStep, parameters, emptyList())).thenReturn(andStep);

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(asList(candidate, andCandidate),
                createScenario(myStep, myAndStep), parameters, stepMonitor);

        // Then
        assertThat(executableSteps.size(), equalTo(2));
    }

    @Test
    void shouldCreateExecutableStepsUponOutcome()
    {
        // Given
        StepCandidate anyCandidate = mock(StepCandidate.class, "anyCandidate");
        StepCandidate successCandidate = mock(StepCandidate.class, "successCandidate");
        StepCandidate failureCandidate = mock(StepCandidate.class, "failureCandidate");
        Step anyStep = mock(Step.class, "anyStep");
        Step successStep = mock(Step.class, "successStep");
        Step failureStep = mock(Step.class, "failureStep");

        String myAnyStep = "my any step";
        when(anyCandidate.matches(myAnyStep, null)).thenReturn(true);
        when(anyCandidate.createMatchedStepUponOutcome(myAnyStep, parameters, emptyList(), Outcome.ANY))
                .thenReturn(anyStep);
        String mySuccessStep = "my success step";
        when(successCandidate.matches(mySuccessStep, null)).thenReturn(true);
        when(successCandidate
                .createMatchedStepUponOutcome(mySuccessStep, parameters, emptyList(), Outcome.SUCCESS))
                .thenReturn(successStep);
        String myFailureStep = "my failure step";
        when(successCandidate.matches(myFailureStep, null)).thenReturn(true);
        when(successCandidate
                .createMatchedStepUponOutcome(myFailureStep, parameters, emptyList(), Outcome.FAILURE))
                .thenReturn(failureStep);

        Lifecycle lifecycle = new Lifecycle(ExamplesTable.EMPTY, emptyList(),
                asList(new Lifecycle.Steps(Outcome.ANY, singletonList(myAnyStep)),
                        new Lifecycle.Steps(Outcome.SUCCESS, singletonList(mySuccessStep)),
                        new Lifecycle.Steps(Outcome.FAILURE, singletonList(myFailureStep))));

        // When
        List<Step> executableSteps = stepCollector.collectLifecycleSteps(
                asList(anyCandidate, successCandidate, failureCandidate), lifecycle, Meta.EMPTY, Scope.SCENARIO,
                new MatchingStepMonitor()).get(Stage.AFTER);

        // Then
        assertThat(executableSteps.size(), equalTo(3));
        assertThat(executableSteps.get(0), equalTo(anyStep));
        assertThat(executableSteps.get(1), equalTo(successStep));
        assertThat(executableSteps.get(2), equalTo(failureStep));
    }

    @Test
    void shouldCreateExecutableStepsUponOutcomeAndScope()
    {
        // Given
        StepCandidate anyCandidate = mock(StepCandidate.class, "anyCandidate");
        StepCandidate successCandidate = mock(StepCandidate.class, "successCandidate");
        StepCandidate failureCandidate = mock(StepCandidate.class, "failureCandidate");
        Step anyStep = mock(Step.class, "anyStep");
        Step successStep = mock(Step.class, "successStep");
        Step failureStep = mock(Step.class, "failureStep");

        String myAnyStep = "my any step";
        when(anyCandidate.matches(myAnyStep, null)).thenReturn(true);
        when(anyCandidate.createMatchedStepUponOutcome(myAnyStep, parameters, emptyList(), Outcome.ANY))
                .thenReturn(anyStep);
        String mySuccessStep = "my success step";
        when(successCandidate.matches(mySuccessStep, null)).thenReturn(true);
        when(successCandidate
                .createMatchedStepUponOutcome(mySuccessStep, parameters, emptyList(), Outcome.SUCCESS))
                .thenReturn(successStep);
        String myFailureStep = "my failure step";
        when(successCandidate.matches(myFailureStep, null)).thenReturn(true);
        when(successCandidate
                .createMatchedStepUponOutcome(myFailureStep, parameters, emptyList(), Outcome.FAILURE))
                .thenReturn(failureStep);

        Scope scope = Scope.STORY;
        Lifecycle lifecycle = new Lifecycle(emptyList(),
                asList(new Lifecycle.Steps(scope, Outcome.ANY, singletonList(myAnyStep)),
                        new Lifecycle.Steps(scope, Outcome.SUCCESS, singletonList(mySuccessStep)),
                        new Lifecycle.Steps(scope, Outcome.FAILURE, singletonList(myFailureStep))));

        // When
        List<Step> executableSteps = stepCollector.collectLifecycleSteps(
                asList(anyCandidate, successCandidate, failureCandidate), lifecycle, Meta.EMPTY, scope,
                new MatchingStepMonitor()).get(Stage.AFTER);

        // Then
        assertThat(executableSteps.size(), equalTo(3));
        assertThat(executableSteps.get(0), equalTo(anyStep));
        assertThat(executableSteps.get(1), equalTo(successStep));
        assertThat(executableSteps.get(2), equalTo(failureStep));
    }

    @Test
    void shouldAddPrioritizedComposedStepsWhenACompositeIsMatched()
    {
        // Given
        StepCandidate compositeCandidate = mock(StepCandidate.class, "compositeCandidate");
        StepCandidate composedCandidate1 = mock(StepCandidate.class, "composedCandidate1");
        StepCandidate composedCandidate2 = mock(StepCandidate.class, "composedCandidate2");
        when(compositeCandidate.getPriority()).thenReturn(3);
        when(composedCandidate1.getPriority()).thenReturn(2);
        when(composedCandidate2.getPriority()).thenReturn(1);
        Step executableComposite = mock(Step.class, "composite");

        String compositeAsText = "my composite step";
        when(compositeCandidate.matches(compositeAsText, null)).thenReturn(true);
        when(compositeCandidate.isComposite()).thenReturn(true);
        when(compositeCandidate.createMatchedStep(compositeAsText, parameters, emptyList())).thenReturn(
                executableComposite);

        // When
        stepCollector.collectScenarioSteps(asList(compositeCandidate, composedCandidate2, composedCandidate1),
                createScenario(compositeAsText), parameters, stepMonitor);

        // Then
        verify(compositeCandidate, times(1)).addComposedSteps(new ArrayList<>(), compositeAsText, parameters,
                asList(compositeCandidate, composedCandidate1, composedCandidate2));
    }

    @Test
    void shouldCreatePendingStepsWhenCandidatesAreNotFound()
    {
        // Given
        String givenPendingStep = "Given a pending step";
        String andGivenPendingStep = "And a given pending step";
        String whenPendingStep = "When yet another pending step";
        String andWhenPendingStep = "And a when pending step";

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(emptyList(),
                createScenario(givenPendingStep, andGivenPendingStep, whenPendingStep, andWhenPendingStep), parameters,
                stepMonitor);
        // Then
        assertThat(executableSteps.size(), equalTo(4));
        assertIsPending(executableSteps.get(0), givenPendingStep, null);
        assertIsPending(executableSteps.get(1), andGivenPendingStep, givenPendingStep);
        assertIsPending(executableSteps.get(2), whenPendingStep, givenPendingStep);
        assertIsPending(executableSteps.get(3), andWhenPendingStep, whenPendingStep);
    }

    @Test
    void shouldCreatePendingStepsWhenCandidatesAreNotMatched()
    {
        // Given
        String givenPendingStep = "Given a pending step";
        String andGivenPendingStep = "And a given pending step";
        String whenPendingStep = "When yet another pending step";
        String andWhenPendingStep = "And a when pending step";
        StepCandidate firstCandidate = mock(StepCandidate.class, "firstCandidate");
        when(firstCandidate.matches(givenPendingStep)).thenReturn(false);
        StepCandidate secondCandidate = mock(StepCandidate.class, "secondCandidate");
        when(secondCandidate.matches(andGivenPendingStep)).thenReturn(false);
        StepCandidate thirdCandidate = mock(StepCandidate.class, "thirdCandidate");
        when(thirdCandidate.matches(whenPendingStep)).thenReturn(false);
        StepCandidate fourthCandidate = mock(StepCandidate.class, "fourthCandidate");
        when(fourthCandidate.matches(andWhenPendingStep)).thenReturn(false);

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(
                asList(firstCandidate, secondCandidate, thirdCandidate, fourthCandidate),
                createScenario(givenPendingStep, andGivenPendingStep, whenPendingStep, andWhenPendingStep), parameters,
                stepMonitor);

        // Then
        assertThat(executableSteps.size(), equalTo(4));
        assertIsPending(executableSteps.get(0), givenPendingStep, null);
        assertIsPending(executableSteps.get(1), andGivenPendingStep, givenPendingStep);
        assertIsPending(executableSteps.get(2), whenPendingStep, givenPendingStep);
        assertIsPending(executableSteps.get(3), andWhenPendingStep, whenPendingStep);
    }

    private void assertIsPending(Step step, String stepAsString, String previousNonAndStep)
    {
        assertThat(step, instanceOf(PendingStep.class));
        PendingStep pendingStep = (PendingStep) step;
        assertThat(pendingStep.stepAsString(), equalTo(stepAsString));
        assertThat(pendingStep.previousNonAndStepAsString(), equalTo(previousNonAndStep));
        StoryReporter reporter = mock(StoryReporter.class);
        Throwable throwable = step.perform(reporter, null).getFailure();
        verify(reporter).beforeStep(
                argThat(arg -> stepAsString.equals(arg.getStepAsString()) && StepExecutionType.PENDING
                        .equals(arg.getExecutionType())));
        assertThat(throwable, instanceOf(PendingStepFound.class));
        assertThat(throwable.getMessage(), equalTo(stepAsString));

    }

    @Test
    void shouldCreateIgnorableSteps()
    {
        // Given
        StepCandidate candidate = mock(StepCandidate.class);
        StoryReporter reporter = mock(StoryReporter.class);

        String stepAsString = "my ignorable step";
        when(candidate.ignore(stepAsString)).thenReturn(true);

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(singletonList(candidate),
                createScenario(stepAsString), parameters, stepMonitor);
        // Then
        assertThat(executableSteps.size(), equalTo(1));
        StepResult result = executableSteps.get(0).perform(reporter, null);
        assertThat(result, instanceOf(Ignorable.class));
        verify(reporter).beforeStep(
                argThat(arg -> stepAsString.equals(arg.getStepAsString()) && StepExecutionType.IGNORABLE
                        .equals(arg.getExecutionType())));
    }

    @Test
    void shouldCreateComment()
    {
        // Given
        StepCandidate candidate = mock(StepCandidate.class);
        StoryReporter reporter = mock(StoryReporter.class);

        String stepAsString = "comment";
        when(candidate.comment(stepAsString)).thenReturn(true);

        // When
        List<Step> executableSteps = stepCollector.collectScenarioSteps(singletonList(candidate),
                createScenario(stepAsString), parameters, stepMonitor);
        // Then
        assertThat(executableSteps.size(), equalTo(1));
        StepResult result = executableSteps.get(0).perform(reporter, null);
        assertThat(result, instanceOf(Comment.class));
        verify(reporter).beforeStep(
                argThat(arg -> stepAsString.equals(arg.getStepAsString()) && StepExecutionType.COMMENT
                        .equals(arg.getExecutionType())));
    }

    @Test
    void shouldCollectBeforeAndAfterScenarioAnnotatedSteps()
    {
        // Given some candidate steps classes with before and after scenario methods
        Meta storyAndScenarioMeta = mock(Meta.class);
        BeforeOrAfterStep bafStep11 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep12 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep21 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep22 = mock(BeforeOrAfterStep.class);
        Step stepBefore1 = mock(Step.class);
        Step stepBefore2 = mock(Step.class);
        Step stepAfter1 = mock(Step.class);
        Step stepAfter2 = mock(Step.class);
        when(bafStep11.createStepWith(storyAndScenarioMeta)).thenReturn(stepBefore1);
        when(bafStep12.createStepWith(storyAndScenarioMeta)).thenReturn(stepBefore2);
        when(bafStep21.createStepUponOutcome(storyAndScenarioMeta)).thenReturn(stepAfter1);
        when(bafStep22.createStepUponOutcome(storyAndScenarioMeta)).thenReturn(stepAfter2);

        // When we collect the list of steps
        List<Step> beforeSteps = stepCollector.collectBeforeScenarioSteps(asList(bafStep11, bafStep12),
                storyAndScenarioMeta);
        List<Step> afterSteps = stepCollector.collectAfterScenarioSteps(asList(bafStep21, bafStep22),
                storyAndScenarioMeta);

        // Then all before and after steps should be added
        assertThat(beforeSteps, equalTo(asList(stepBefore1, stepBefore2)));
        assertThat(afterSteps, equalTo(asList(stepAfter1, stepAfter2)));
    }

    @Test
    void shouldCollectBeforeAndAfterStoryAnnotatedSteps()
    {
        // Given some candidate steps classes with before and after story methods
        Story story = new Story();
        Meta storyMeta = story.getMeta();

        BeforeOrAfterStep bafStep11 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep21 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep12 = mock(BeforeOrAfterStep.class);
        BeforeOrAfterStep bafStep22 = mock(BeforeOrAfterStep.class);
        Step stepBefore1 = mock(Step.class);
        Step stepBefore2 = mock(Step.class);
        Step stepAfter1 = mock(Step.class);
        Step stepAfter2 = mock(Step.class);

        when(bafStep11.createStepWith(storyMeta)).thenReturn(stepBefore1);
        when(bafStep21.createStepWith(storyMeta)).thenReturn(stepBefore2);
        when(bafStep12.createStepWith(storyMeta)).thenReturn(stepAfter1);
        when(bafStep22.createStepWith(storyMeta)).thenReturn(stepAfter2);

        // When we collect the list of steps
        List<Step> beforeSteps = stepCollector.collectBeforeOrAfterStorySteps(asList(bafStep11, bafStep21), storyMeta);
        List<Step> afterSteps = stepCollector.collectBeforeOrAfterStorySteps(asList(bafStep12, bafStep22), storyMeta);

        // Then all before and after steps should be added
        assertThat(beforeSteps, equalTo(asList(stepBefore1, stepBefore2)));
        assertThat(afterSteps, equalTo(asList(stepAfter1, stepAfter2)));
    }

    @Test
    void shouldCollectBeforeAndAfterStoriesAnnotatedSteps()
    {
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

        when(bafStep11.createStep()).thenReturn(stepBefore1);
        when(bafStep21.createStep()).thenReturn(stepBefore2);
        when(bafStep12.createStep()).thenReturn(stepAfter1);
        when(bafStep22.createStep()).thenReturn(stepAfter2);
        when(steps1.listBeforeStories()).thenReturn(singletonList(bafStep11));
        when(steps1.listAfterStories()).thenReturn(singletonList(bafStep12));
        when(steps2.listBeforeStories()).thenReturn(singletonList(bafStep21));
        when(steps2.listAfterStories()).thenReturn(singletonList(bafStep22));

        // When we collect the list of steps
        List<Step> steps = stepCollector.collectBeforeOrAfterStoriesSteps(
                asList(bafStep11, bafStep12, bafStep21, bafStep22));

        // Then all before and after steps should be added
        assertThat(steps, equalTo(asList(stepBefore1, stepAfter1, stepBefore2, stepAfter2)));
    }

    @Test
    void shouldSortCandidateStepsByPriorityByDefault()
    {
        // Given some candidate steps classes
        // and some methods split across them
        StepCandidate candidate1 = mock(StepCandidate.class);
        StepCandidate candidate2 = mock(StepCandidate.class);
        StepCandidate candidate3 = mock(StepCandidate.class);
        StepCandidate candidate4 = mock(StepCandidate.class);
        Step step1 = mock(Step.class);
        Step step2 = mock(Step.class);
        Step step3 = mock(Step.class);
        Step step4 = mock(Step.class);

        // all matching the same step string with different priorities
        String stepAsString = "Given a step";
        when(candidate1.matches(stepAsString, null)).thenReturn(true);
        when(candidate2.matches(stepAsString, null)).thenReturn(true);
        when(candidate3.matches(stepAsString, null)).thenReturn(true);
        when(candidate4.matches(stepAsString, null)).thenReturn(true);
        when(candidate1.getPriority()).thenReturn(1);
        when(candidate2.getPriority()).thenReturn(2);
        when(candidate3.getPriority()).thenReturn(3);
        when(candidate4.getPriority()).thenReturn(4);
        when(candidate1.createMatchedStep(stepAsString, parameters, emptyList())).thenReturn(step1);
        when(candidate2.createMatchedStep(stepAsString, parameters, emptyList())).thenReturn(step2);
        when(candidate3.createMatchedStep(stepAsString, parameters, emptyList())).thenReturn(step3);
        when(candidate4.createMatchedStep(stepAsString, parameters, emptyList())).thenReturn(step4);

        // When we collect the list of steps
        List<Step> steps = stepCollector.collectScenarioSteps(asList(candidate1, candidate2, candidate3, candidate4),
                createScenario(stepAsString), parameters, stepMonitor);

        // Then the step with highest priority is returned
        assertThat(step4, equalTo(steps.get(0)));
    }

    @Test
    void shouldPrioritiseCandidateStepsByInjectableStrategy()
    {
        // Given some candidate steps classes
        // and some methods split across them
        StepCandidate candidate1 = mock(StepCandidate.class);
        StepCandidate candidate2 = mock(StepCandidate.class);
        StepCandidate candidate3 = mock(StepCandidate.class);
        StepCandidate candidate4 = mock(StepCandidate.class);
        Step step1 = mock(Step.class);
        Step step2 = mock(Step.class);
        Step step3 = mock(Step.class);
        Step step4 = mock(Step.class);

        // all matching the same step string with different priorities
        String stepAsString = "Given a step";
        when(candidate1.matches(stepAsString, null)).thenReturn(true);
        when(candidate2.matches(stepAsString, null)).thenReturn(true);
        when(candidate3.matches(stepAsString, null)).thenReturn(true);
        when(candidate4.matches(stepAsString, null)).thenReturn(true);
        when(candidate1.getPatternAsString()).thenReturn("Given I do something");
        when(candidate2.getPatternAsString()).thenReturn("When I do something ");
        when(candidate3.getPatternAsString()).thenReturn("Then I do something");
        when(candidate4.getPatternAsString()).thenReturn("And I do something");
        when(candidate1.createMatchedStep(stepAsString, parameters, emptyList())).thenReturn(step1);
        when(candidate2.createMatchedStep(stepAsString, parameters, emptyList())).thenReturn(step2);
        when(candidate3.createMatchedStep(stepAsString, parameters, emptyList())).thenReturn(step3);
        when(candidate4.createMatchedStep(stepAsString, parameters, emptyList())).thenReturn(step4);

        StepCollector stepCollector = new MarkUnmatchedStepsAsPending(new StepFinder(new ByLevenshteinDistance()));
        List<Step> steps = stepCollector.collectScenarioSteps(asList(candidate1, candidate2, candidate3, candidate4),
                createScenario(stepAsString), parameters, stepMonitor);

        // Then the step with highest priority is returned
        assertThat(step4, equalTo(steps.get(0)));
    }

    @Test
    void afterScenarioStepsShouldBeInReverseOrder()
    {
        List<CandidateSteps> steps = new ArrayList<>();
        steps.add(new ClassWithMethodsAandB());
        steps.add(new ClassWithMethodsCandD());
        String stepsAsString = steps.toString(); // includes object ID numbers from JVM

        Map<ScenarioType, List<BeforeOrAfterStep>> beforeScenario = new EnumMap<>(ScenarioType.class);
        Map<ScenarioType, List<BeforeOrAfterStep>> afterScenario = new EnumMap<>(ScenarioType.class);
        for (ScenarioType type : ScenarioType.values())
        {
            beforeScenario.put(type, new ArrayList<>());
            afterScenario.put(type, new ArrayList<>());
        }
        for (CandidateSteps step : steps)
        {
            for (ScenarioType scenarioType : ScenarioType.values())
            {
                beforeScenario.get(scenarioType).addAll(step.listBeforeScenario().get(scenarioType));
                afterScenario.get(scenarioType).addAll(step.listAfterScenario().get(scenarioType));
            }
        }

        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<Step> subsetBefore = stepCollector.collectBeforeScenarioSteps(beforeScenario.get(scenarioType),
                Meta.EMPTY);
        assertThat(subsetBefore.toString(), containsString("ClassWithMethodsAandB.a()"));
        assertThat(subsetBefore.toString(), containsString("ClassWithMethodsCandD.c()"));

        assertThat(stepsAsString, equalTo(steps.toString())); // steps have not been mutated.

        List<Step> subsetAfter = stepCollector.collectAfterScenarioSteps(afterScenario.get(scenarioType), Meta.EMPTY);
        assertThat(subsetAfter.toString(), containsString("ClassWithMethodsCandD.d()"));
        assertThat(subsetAfter.toString(), containsString("ClassWithMethodsAandB.b()"));

    }

    @Test
    void shouldInvokeBeforeOrAfterScenarioWithParameter()
    {
        BeforeOrAfterScenarioWithParameterSteps steps = new BeforeOrAfterScenarioWithParameterSteps();
        Meta meta = beforeAndAfterMeta();

        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<Step> beforeSteps = stepCollector.collectBeforeScenarioSteps(steps.listBeforeScenario().get(scenarioType),
                meta);
        beforeSteps.get(0).perform(null, null);
        assertThat(steps.value, equalTo("before"));

        List<Step> afterSteps = stepCollector.collectAfterScenarioSteps(steps.listAfterScenario().get(scenarioType),
                meta);
        afterSteps.get(0).perform(null, null);
        assertThat(steps.value, equalTo("after"));
    }

    @Test
    void shouldInvokeBeforeOrAfterScenarioWithParameterAndException()
    {
        BeforeOrAfterScenarioWithParameterAndExceptionSteps steps =
                new BeforeOrAfterScenarioWithParameterAndExceptionSteps();
        Meta meta = beforeAndAfterMeta();

        UUIDExceptionWrapper failureOccurred = new UUIDExceptionWrapper();

        ScenarioType scenarioType = ScenarioType.NORMAL;
        List<Step> beforeSteps = stepCollector.collectBeforeScenarioSteps(steps.listBeforeScenario().get(scenarioType),
                meta);
        beforeSteps.get(0).doNotPerform(null, failureOccurred);
        assertThat(steps.value, equalTo("before"));
        assertThat(steps.exception, equalTo(failureOccurred));

        List<Step> afterSteps = stepCollector.collectAfterScenarioSteps(steps.listAfterScenario().get(scenarioType),
                meta);
        failureOccurred = new UUIDExceptionWrapper();
        afterSteps.get(0).doNotPerform(null, failureOccurred);
        assertThat(steps.value, equalTo("after"));
        assertThat(steps.exception, equalTo(failureOccurred));
    }

    @Test
    void shouldInvokeBeforeOrAfterStoryWithParameter()
    {
        BeforeOrAfterStoryWithParameter steps = new BeforeOrAfterStoryWithParameter();
        boolean givenStory = false;
        Meta storyMeta = beforeAndAfterMeta();

        List<Step> beforeSteps = stepCollector.collectBeforeOrAfterStorySteps(steps.listBeforeStory(givenStory),
                storyMeta);
        beforeSteps.get(0).perform(null, null);
        assertThat(steps.value, equalTo("before"));

        List<Step> afterSteps = stepCollector.collectBeforeOrAfterStorySteps(steps.listAfterStory(givenStory),
                storyMeta);
        afterSteps.get(0).perform(null, null);
        assertThat(steps.value, equalTo("after"));
    }

    @Test
    void shouldInvokeBeforeOrAfterStoryWithParameterAndException()
    {
        BeforeOrAfterStoryWithParameterAndExceptionSteps steps = new BeforeOrAfterStoryWithParameterAndExceptionSteps();
        boolean givenStory = false;
        Meta storyMeta = beforeAndAfterMeta();

        List<Step> beforeSteps = stepCollector.collectBeforeOrAfterStorySteps(steps.listBeforeStory(givenStory),
                storyMeta);
        UUIDExceptionWrapper failureOccurred = new UUIDExceptionWrapper();
        beforeSteps.get(0).doNotPerform(null, failureOccurred);
        assertThat(steps.value, equalTo("before"));
        assertThat(steps.exception, equalTo(failureOccurred));

        List<Step> afterSteps = stepCollector.collectBeforeOrAfterStorySteps(steps.listAfterStory(givenStory),
                storyMeta);
        failureOccurred = new UUIDExceptionWrapper();
        afterSteps.get(0).doNotPerform(null, failureOccurred);
        assertThat(steps.value, equalTo("after"));
        assertThat(steps.exception, equalTo(failureOccurred));
    }

    private Scenario createScenario(String... stepsAsStrings)
    {
        return new Scenario(asList(stepsAsStrings));
    }

    private Meta beforeAndAfterMeta()
    {
        Properties properties = new Properties();
        properties.put("before", "before");
        properties.put("after", "after");
        return new Meta(properties);
    }

    public static class ClassWithMethodsAandB extends Steps
    {
        @BeforeScenario(uponType = ScenarioType.NORMAL)
        public void a()
        {
        }

        @AfterScenario(uponType = ScenarioType.NORMAL)
        public void b()
        {
        }
    }

    public static class ClassWithMethodsCandD extends Steps
    {
        @BeforeScenario(uponType = ScenarioType.NORMAL)
        public void c()
        {
        }

        @AfterScenario(uponType = ScenarioType.NORMAL)
        public void d()
        {
        }
    }

    public static class BeforeOrAfterScenarioWithParameterSteps extends Steps
    {
        private String value;

        @BeforeScenario(uponType = ScenarioType.NORMAL)
        public void beforeScenario(@Named("before") String before)
        {
            this.value = before;
        }

        @AfterScenario(uponType = ScenarioType.NORMAL)
        public void afterScenario(@Named("after") String after)
        {
            this.value = after;
        }
    }

    public static class BeforeOrAfterScenarioWithParameterAndExceptionSteps extends Steps
    {
        private String value;
        private UUIDExceptionWrapper exception;

        @BeforeScenario(uponType = ScenarioType.NORMAL)
        public void beforeScenario(@Named("before") String before, UUIDExceptionWrapper exception)
        {
            this.value = before;
            this.exception = exception;
        }

        @AfterScenario(uponType = ScenarioType.NORMAL)
        public void afterScenario(@Named("after") String after, UUIDExceptionWrapper exception)
        {
            this.value = after;
            this.exception = exception;
        }
    }

    public static class BeforeOrAfterStoryWithParameter extends Steps
    {
        private String value;

        @BeforeStory
        public void beforeStory(@Named("before") String before)
        {
            this.value = before;
        }

        @AfterStory
        public void afterStory(@Named("after") String after)
        {
            this.value = after;
        }
    }

    public static class BeforeOrAfterStoryWithParameterAndExceptionSteps extends Steps
    {
        private String value;
        private UUIDExceptionWrapper exception;

        @BeforeStory
        public void beforeStory(@Named("before") String before, UUIDExceptionWrapper exception)
        {
            this.value = before;
            this.exception = exception;
        }

        @AfterStory
        public void afterStory(@Named("after") String after, UUIDExceptionWrapper exception)
        {
            this.value = after;
            this.exception = exception;
        }
    }
}
