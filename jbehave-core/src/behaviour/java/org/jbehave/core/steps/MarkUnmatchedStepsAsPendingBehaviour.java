package org.jbehave.core.steps;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector.Stage;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MarkUnmatchedStepsAsPendingBehaviour {

    private Map<String, String> tableRow = new HashMap<String, String>();

    @Test
    public void shouldMatchUpStepsAndScenarioDefinitionToCreateExecutableSteps() {
        // Given
        MarkUnmatchedStepsAsPending stepCollector = new MarkUnmatchedStepsAsPending();
        
        CandidateStep candidate = mock(CandidateStep.class);
        CandidateSteps steps = mock(Steps.class);
        Step executableStep = mock(Step.class);

        when(candidate.matches("my step")).thenReturn(true);
        when(candidate.createStep("my step", tableRow)).thenReturn(executableStep);
        when(steps.getSteps()).thenReturn(new CandidateStep[] { candidate });

        // When
        List<Step> executableSteps = stepCollector
                .collectStepsFrom(asList(steps), new Scenario(asList("my step")), tableRow);

        // Then
        assertThat(executableSteps.size(), equalTo(1));
        assertThat(executableSteps.get(0), equalTo(executableStep));
    }

    @Test
    public void shouldProvidePendingStepsForAnyStepsWhichAreNotAvailable() {
        // Given
        MarkUnmatchedStepsAsPending stepCollector = new MarkUnmatchedStepsAsPending();

        CandidateStep candidate = mock(CandidateStep.class);
        CandidateSteps steps = mock(Steps.class);

        when(candidate.matches("my step")).thenReturn(false);
        when(steps.getSteps()).thenReturn(new CandidateStep[] { candidate });

        // When
        List<Step> executableSteps = stepCollector
                .collectStepsFrom(asList(steps), new Scenario(asList("my step")), tableRow);
        // Then
        assertThat(executableSteps.size(), equalTo(1));
        StepResult result = executableSteps.get(0).perform();
        assertThat(result.getThrowable().getMessage(), equalTo("Pending: my step"));
    }

    @Test
    public void shouldPrependBeforeScenarioAndAppendAfterScenarioAnnotatedSteps() {
        // Given some steps classes which run different steps before and after
        // stories
        CandidateSteps steps1 = mock(Steps.class);
        CandidateSteps steps2 = mock(Steps.class);
        Step stepBefore1 = mock(Step.class);
        Step stepBefore2 = mock(Step.class);
        Step stepAfter1 = mock(Step.class);
        Step stepAfter2 = mock(Step.class);

        when(steps1.runBeforeScenario()).thenReturn(asList(stepBefore1));
        when(steps2.runBeforeScenario()).thenReturn(asList(stepBefore2));
        when(steps1.runAfterScenario()).thenReturn(asList(stepAfter1));
        when(steps2.runAfterScenario()).thenReturn(asList(stepAfter2));

        // And which have a 'normal' step that matches our core
        CandidateStep candidate = mock(CandidateStep.class);
        Step normalStep = mock(Step.class);

        when(candidate.matches("my step")).thenReturn(true);
        when(candidate.createStep("my step", tableRow)).thenReturn(normalStep);
        when(steps1.getSteps()).thenReturn(new CandidateStep[] { candidate });
        when(steps2.getSteps()).thenReturn(new CandidateStep[] {});

        // When we collect the list of steps
        MarkUnmatchedStepsAsPending stepCollector = new MarkUnmatchedStepsAsPending();
        List<Step> executableSteps = stepCollector.collectStepsFrom(asList(steps1, steps2), new Scenario(asList("my step")), tableRow
        );

        // Then all before and after steps should be added
        assertThat(executableSteps, equalTo(asList(stepBefore2, stepBefore1, normalStep, stepAfter1, stepAfter2)));
    }

    @Test
    public void shouldReturnBeforeAndAfterStoryAnnotatedSteps() {
        // Given some steps classes which run different steps before and after
        // story
        CandidateSteps steps1 = mock(Steps.class);
        CandidateSteps steps2 = mock(Steps.class);
        Step stepBefore1 = mock(Step.class);
        Step stepBefore2 = mock(Step.class);
        Step stepAfter1 = mock(Step.class);
        Step stepAfter2 = mock(Step.class);

        boolean embeddedStory = false;
        when(steps1.runBeforeStory(embeddedStory)).thenReturn(asList(stepBefore1));
        when(steps2.runBeforeStory(embeddedStory)).thenReturn(asList(stepBefore2));
        when(steps1.runAfterStory(embeddedStory)).thenReturn(asList(stepAfter1));
        when(steps2.runAfterStory(embeddedStory)).thenReturn(asList(stepAfter2));

        // When we collect the list of steps
        MarkUnmatchedStepsAsPending stepCollector = new MarkUnmatchedStepsAsPending();
        List<Step> beforeSteps = stepCollector.collectStepsFrom(asList(steps1, steps2), new Story(new Scenario()), Stage.BEFORE,
                embeddedStory);
        List<Step> afterSteps = stepCollector.collectStepsFrom(asList(steps1, steps2), new Story(new Scenario()), Stage.AFTER,
                embeddedStory);

        // Then all before and after steps should be added
        assertThat(beforeSteps, equalTo(asList(stepBefore1, stepBefore2)));
        assertThat(afterSteps, equalTo(asList(stepAfter1, stepAfter2)));
    }

    @Test
    public void shouldPrioritiseAnnotatedSteps() {
        // Given some Steps classes  
        // and some candidate steps split across them

        CandidateSteps steps1 = mock(Steps.class);
        CandidateSteps steps2 = mock(Steps.class);
        CandidateStep candidate1 = mock(CandidateStep.class);
        CandidateStep candidate2 = mock(CandidateStep.class);
        CandidateStep candidate3 = mock(CandidateStep.class);
        CandidateStep candidate4 = mock(CandidateStep.class);
        Step step1 = mock(Step.class);
        Step step2 = mock(Step.class);
        Step step3 = mock(Step.class);
        Step step4 = mock(Step.class);

        when(steps1.getSteps()).thenReturn(new CandidateStep[]{candidate1, candidate2});
        when(steps2.getSteps()).thenReturn(new CandidateStep[]{candidate3, candidate4});
        
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
        when(candidate1.createStep(stepAsString, tableRow)).thenReturn(step1);
        when(candidate2.createStep(stepAsString, tableRow)).thenReturn(step2);
        when(candidate3.createStep(stepAsString, tableRow)).thenReturn(step3);
        when(candidate4.createStep(stepAsString, tableRow)).thenReturn(step4);
        
        // When we collect the list of steps
        MarkUnmatchedStepsAsPending stepCollector = new MarkUnmatchedStepsAsPending();
        List<Step> steps = stepCollector.collectStepsFrom(asList(steps1, steps2), new Scenario(asList(stepAsString)), tableRow
        );

        // Then the step with highest priority is returned
        assertThat(step4, equalTo(steps.get(0)));
    }

}
