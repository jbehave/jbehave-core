package org.jbehave.core.steps;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

/**
 * Represents the strategy for the creation of a list of executable {@link Step}s from a
 * given story or scenario matching a list of {@link CandidateSteps}.
 */
public interface StepCreator {

    enum Stage {
        BEFORE, AFTER
    }

    List<Step> createStepsFrom(List<CandidateSteps> candidateSteps, Story story, Stage stage, boolean embeddedStory);

    List<Step> createStepsFrom(List<CandidateSteps> candidateSteps, Scenario scenario, Map<String, String> tableRow);

}
