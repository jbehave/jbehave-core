package org.jbehave.core.steps;

import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Scope;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;

/**
 * Represents the strategy for the collection of executable {@link Step}s from a story or scenario matching a list of
 * {@link StepCandidate}s. It also collects the steps to run at before/after stages.
 */
public interface StepCollector {

    enum Stage {
        BEFORE, AFTER
    }

    /**
     * Creates steps to be executed either before or after stories.
     *
     * @param beforeOrAfterStoriesSteps the {@link BeforeOrAfterStep}s
     * @return A List of the executable {@link Step}s
     */
    List<Step> collectBeforeOrAfterStoriesSteps(List<BeforeOrAfterStep> beforeOrAfterStoriesSteps);

    /**
     * Creates steps to be executed either before or after story.
     *
     * @param beforeOrAfterStorySteps the {@link BeforeOrAfterStep}s
     * @param storyMeta the story {@link Meta} parameters
     * @return A List of the executable {@link Step}s
     */
    List<Step> collectBeforeOrAfterStorySteps(List<BeforeOrAfterStep> beforeOrAfterStorySteps, Meta storyMeta);

    /**
     * Creates steps to be executed before scenario.
     *
     * @param beforeScenarioSteps the {@link BeforeOrAfterStep}s
     * @param storyAndScenarioMeta the story and scenario {@link Meta} parameters
     * @return A List of the executable {@link Step}s
     */
    List<Step> collectBeforeScenarioSteps(List<BeforeOrAfterStep> beforeScenarioSteps, Meta storyAndScenarioMeta);

    /**
     * Creates steps to be executed after scenario.
     * @param afterScenarioSteps the {@link BeforeOrAfterStep}s
     * @param storyAndScenarioMeta the story and scenario {@link Meta} parameters
     * @return A List of the executable {@link Step}s
     */
    List<Step> collectAfterScenarioSteps(List<BeforeOrAfterStep> afterScenarioSteps, Meta storyAndScenarioMeta);

    /**
     * Collects all lifecycle steps to execute per {@link Stage} of execution
     *
     * @param stepCandidates the {@link StepCandidate}s
     * @param lifecycle the {@link Lifecycle}
     * @param storyAndScenarioMeta the story and scenario {@link Meta} parameters
     * @param scope the {@link Scope} of the lifecycle steps
     * @param parameters the parameters
     * @param stepMonitor the {@link StepMonitor}
     * @return A List of executable {@link Step}s
     */
    Map<Stage, List<Step>> collectLifecycleSteps(List<StepCandidate> stepCandidates, Lifecycle lifecycle,
            Meta storyAndScenarioMeta, Scope scope, Map<String, String> parameters, StepMonitor stepMonitor);

    /**
     * Collects all of the {@link Step}s to execute for a scenario.
     *
     * @param stepCandidates the {@link StepCandidate}
     * @param scenario the {@link Scenario}
     * @param parameters the parameters
     * @param stepMonitor the {@link StepMonitor}
     * @return A List of executable {@link Step}s
     */
    List<Step> collectScenarioSteps(List<StepCandidate> stepCandidates, Scenario scenario,
            Map<String, String> parameters, StepMonitor stepMonitor);
}
