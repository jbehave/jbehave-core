package org.jbehave.core.steps;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCreator.Stage;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MarkUnmatchedStepsAsPendingBehaviour {

    private Map<String, String> tableRow = new HashMap<String, String>();

    @Test
    public void shouldMatchUpStepsAndScenarioDefinitionToCreateExecutableSteps() {
        // Given
        MarkUnmatchedStepsAsPending stepCreator = new MarkUnmatchedStepsAsPending();

        CandidateStep candidate = mock(CandidateStep.class);
        CandidateSteps steps = mock(Steps.class);
        Step executableStep = mock(Step.class);

        when(candidate.matches("my step")).thenReturn(true);
        when(candidate.createFrom(tableRow, "my step")).thenReturn(executableStep);
        when(steps.getSteps()).thenReturn(new CandidateStep[] { candidate });

        // When
        List<Step> executableSteps = stepCreator
                .createStepsFrom(asList(steps), new Scenario(asList("my step")), tableRow);

        // Then
        ensureThat(executableSteps.size(), equalTo(1));
        ensureThat(executableSteps.get(0), equalTo(executableStep));
    }

    @Test
    public void shouldProvidePendingStepsForAnyStepsWhichAreNotAvailable() {
        // Given
        MarkUnmatchedStepsAsPending stepCreator = new MarkUnmatchedStepsAsPending();

        CandidateStep candidate = mock(CandidateStep.class);
        CandidateSteps steps = mock(Steps.class);

        when(candidate.matches("my step")).thenReturn(false);
        when(steps.getSteps()).thenReturn(new CandidateStep[] { candidate });

        // When
        List<Step> executableSteps = stepCreator
                .createStepsFrom(asList(steps), new Scenario(asList("my step")), tableRow);
        // Then
        ensureThat(executableSteps.size(), equalTo(1));
        StepResult result = executableSteps.get(0).perform();
        ensureThat(result.getThrowable().getMessage(), equalTo("Pending: my step"));
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
        when(candidate.createFrom(tableRow, "my step")).thenReturn(normalStep);
        when(steps1.getSteps()).thenReturn(new CandidateStep[] { candidate });
        when(steps2.getSteps()).thenReturn(new CandidateStep[] {});

        // When we create the series of steps for the core
        MarkUnmatchedStepsAsPending stepCreator = new MarkUnmatchedStepsAsPending();
        List<Step> executableSteps = stepCreator.createStepsFrom(asList(steps1, steps2), new Scenario(asList("my step")), tableRow
        );

        // Then all before and after steps should be added
        ensureThat(executableSteps, equalTo(asList(stepBefore2, stepBefore1, normalStep, stepAfter1, stepAfter2)));
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

        // When we create the series of steps for the core
        MarkUnmatchedStepsAsPending stepCreator = new MarkUnmatchedStepsAsPending();
        List<Step> beforeSteps = stepCreator.createStepsFrom(asList(steps1, steps2), new Story(new Scenario()), Stage.BEFORE,
                embeddedStory);
        List<Step> afterSteps = stepCreator.createStepsFrom(asList(steps1, steps2), new Story(new Scenario()), Stage.AFTER,
                embeddedStory);

        // Then all before and after steps should be added
        ensureThat(beforeSteps, equalTo(asList(stepBefore1, stepBefore2)));
        ensureThat(afterSteps, equalTo(asList(stepAfter1, stepAfter2)));
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
        when(candidate1.createFrom(tableRow, stepAsString)).thenReturn(step1);
        when(candidate2.createFrom(tableRow, stepAsString)).thenReturn(step2);
        when(candidate3.createFrom(tableRow, stepAsString)).thenReturn(step3);
        when(candidate4.createFrom(tableRow, stepAsString)).thenReturn(step4);
        
        // When we create the series of steps for the core
        MarkUnmatchedStepsAsPending stepCreator = new MarkUnmatchedStepsAsPending();
        List<Step> steps = stepCreator.createStepsFrom(asList(steps1, steps2), new Scenario(asList(stepAsString)), tableRow
        );

        // Then the step with highest priority is returned
        ensureThat(step4, equalTo(steps.get(0)));
    }

}
