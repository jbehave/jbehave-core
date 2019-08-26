package org.jbehave.core.steps;

import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.*;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

/**
 * Represents the strategy for the collection of executable {@link Step}s from a
 * story or scenario matching a list of {@link CandidateSteps}. It also collects the 
 * steps to run at before/after stages.
 */
public interface StepCollector {

    public enum Stage {
        BEFORE, AFTER
    }

    /**
     * Collects all of the {@link BeforeStories} or {@link AfterStories} steps to execute.
     * 
     * @param candidateSteps
     * @param stage the {@link Stage} of execution
     * @return A List of executable {@link Step}s 
     */
    List<Step> collectBeforeOrAfterStoriesSteps(List<CandidateSteps> candidateSteps, Stage stage);

    /**
     * Collects all of the {@link BeforeStory} or {@link AfterStory} steps to execute.
     * 
     * @param candidateSteps the {@link CandidateSteps}.
     * @param story the {@link Story}.
     * @param stage the {@link Stage} of execution
     * @param givenStory whether {@link Story} is a given story
     * @return A List of executable {@link Step}s 
     */
    List<Step> collectBeforeOrAfterStorySteps(List<CandidateSteps> candidateSteps, Story story, Stage stage, boolean givenStory);

    /**
     * Collects all of the {@link BeforeScenario} or {@link AfterScenario} steps to execute.
     * 
     *
     * @param candidateSteps the {@link CandidateSteps}.
     * @param storyAndScenarioMeta the story and scenario {@link Meta} parameters
     * @param type the ScenarioType
     * @return A List of executable {@link Step}s 
     */
    List<Step> collectBeforeOrAfterScenarioSteps(List<CandidateSteps> candidateSteps, Meta storyAndScenarioMeta, Stage stage, ScenarioType type);

    /**
     * @deprecated Use {@link #collectLifecycleSteps(List, Lifecycle, Meta, Scope)}
     *
     * Collects all lifecycle steps to execute for default scope
     *
     * @param candidateSteps the {@link CandidateSteps}.
     * @param lifecycle the {@link Lifecycle}
     * @param storyAndScenarioMeta the story and scenario {@link Meta} parameters
     * @param stage the {@link Stage} of execution
     * @return A List of executable {@link Step}s
     */
    @Deprecated
    List<Step> collectLifecycleSteps(List<CandidateSteps> candidateSteps, Lifecycle lifecycle, Meta storyAndScenarioMeta, Stage stage);

    /**
     * @deprecated Use {@link #collectLifecycleSteps(List, Lifecycle, Meta, Scope)}
     *
     * Collects all lifecycle steps to execute
     *
     * @param candidateSteps the {@link CandidateSteps}.
     * @param lifecycle the {@link Lifecycle}
     * @param storyAndScenarioMeta the story and scenario {@link Meta} parameters
     * @param stage the {@link Stage} of execution
     * @param scope the {@link Scope} of the lifecycle steps
     * @return A List of executable {@link Step}s
     */
    @Deprecated
    List<Step> collectLifecycleSteps(List<CandidateSteps> candidateSteps, Lifecycle lifecycle, Meta storyAndScenarioMeta, Stage stage, Scope scope);

    /**
     * Collects all lifecycle steps to execute per {@link Stage} of execution
     *
     * @param candidateSteps the {@link CandidateSteps}.
     * @param lifecycle the {@link Lifecycle}
     * @param storyAndScenarioMeta the story and scenario {@link Meta} parameters
     * @param scope the {@link Scope} of the lifecycle steps
     * @return A List of executable {@link Step}s
     */
    Map<Stage, List<Step>> collectLifecycleSteps(List<CandidateSteps> candidateSteps, Lifecycle lifecycle, Meta storyAndScenarioMeta, Scope scope);

    /**
     * Collects all of the {@link Step}s to execute for a scenario.
     * 
     * @param candidateSteps the {@link CandidateSteps}.
     * @param scenario the {@link Scenario}.
     * @param parameters the parameters.
     * @return A List of executable {@link Step}s 
     */
    List<Step> collectScenarioSteps(List<CandidateSteps> candidateSteps, Scenario scenario, Map<String, String> parameters);

    /**
     * Collects all of the {@link Step}s to execute for a scenario.
     * 
     * @param candidateSteps the {@link CandidateSteps}.
     * @param scenario the {@link Scenario}.
     * @param parameters the parameters.
     * @param stepMonitor the {@link StepMonitor}.
     * @return A List of executable {@link Step}s 
     */
    List<Step> collectScenarioSteps(List<CandidateSteps> candidateSteps, Scenario scenario, Map<String, String> parameters, StepMonitor stepMonitor);

}
