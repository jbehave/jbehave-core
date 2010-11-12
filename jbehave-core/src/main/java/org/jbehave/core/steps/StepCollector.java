package org.jbehave.core.steps;

import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

/**
 * Represents the strategy for the collection of executable {@link Step}s from a
 * given story or scenario matching a list of {@link CandidateSteps}.
 */
public interface StepCollector {
    enum Stage {
        BEFORE, AFTER
    }

    /**
     * Collects all of the {@link BeforeStories} or {@link AfterStories} steps to execute.
     * 
     * @param candidateSteps
     * @param stage
     * @return
     */
    List<Step> collectBeforeOrAfterStoriesSteps(List<CandidateSteps> candidateSteps, Stage stage);

    /**
     * Collects all of the {@link BeforeStory} or {@link AfterStory} steps to execute.
     * 
     * @param candidateSteps the {@link CandidateSteps}.
     * @param story the {@link Story}.
     * @param stage the {@link Stage}.
     * @param givenStory
     * @return
     */
    List<Step> collectBeforeOrAfterStorySteps(List<CandidateSteps> candidateSteps, Story story, Stage stage, boolean givenStory);

    /**
     * Collects all of the {@link BeforeScenario} or {@link AfterScenario} steps to execute.
     * 
     * @param candidateSteps the {@link CandidateSteps}.
     * @param parameters the parameters.
     * @return the {@link Step}s to execute.
     */
    List<Step> collectBeforeOrAfterScenarioSteps(List<CandidateSteps> candidateSteps, Stage stage);

    /**
     * Collects all of the {@link Step}s to execute for a scenario.
     * 
     * @param candidateSteps the {@link CandidateSteps}.
     * @param scenario the {@link Scenario}.
     * @param parameters the parameters.
     * @return the {@link Step}s to execute.
     */
    List<Step> collectScenarioSteps(List<CandidateSteps> candidateSteps, Scenario scenario, Map<String, String> parameters);
}
