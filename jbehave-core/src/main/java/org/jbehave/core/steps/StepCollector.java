package org.jbehave.core.steps;

import java.util.List;
import java.util.Map;

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

    List<Step> collectStepsFrom(List<CandidateSteps> candidateSteps, Story story, Stage stage, boolean embeddedStory);

    List<Step> collectStepsFrom(List<CandidateSteps> candidateSteps, Scenario scenario, Map<String, String> tableRow);

}
