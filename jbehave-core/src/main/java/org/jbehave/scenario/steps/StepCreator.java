package org.jbehave.scenario.steps;

import java.util.Map;

import org.jbehave.scenario.definition.ScenarioDefinition;
import org.jbehave.scenario.definition.StoryDefinition;

/**
 * Represents the strategy for the creation of executable {@link Step}s from a
 * given story or scenario definition matching a list of {@link CandidateSteps}.
 */
public interface StepCreator {

    enum Stage {
        BEFORE, AFTER
    };

    Step[] createStepsFrom(StoryDefinition storyDefinition, Stage stage, boolean embeddedStory, CandidateSteps... candidateSteps);

    Step[] createStepsFrom(ScenarioDefinition scenarioDefinition, Map<String, String> tableRow,
            CandidateSteps... candidateSteps);

}
