package org.jbehave.core.model;

import java.util.List;

import static java.util.Collections.unmodifiableList;

import org.jbehave.core.steps.StepType;

/**
 * @author Valery Yatsynovich
 */
public class CompositeStep {

    private StepType stepType;
    private String stepWithoutStartingWord;
    private List<String> steps;

    public CompositeStep(StepType stepType, String stepWithoutStartingWord, List<String> steps) {
        this.stepType = stepType;
        this.stepWithoutStartingWord = stepWithoutStartingWord;
        this.steps = steps;
    }

    public StepType getStepType() {
        return stepType;
    }

    public String getStepWithoutStartingWord() {
        return stepWithoutStartingWord;
    }

    public List<String> getSteps() {
        return unmodifiableList(steps);
    }
}
