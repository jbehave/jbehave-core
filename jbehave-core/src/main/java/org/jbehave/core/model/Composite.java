package org.jbehave.core.model;

import java.util.List;

import static java.util.Collections.unmodifiableList;

import org.jbehave.core.steps.StepType;

/**
 * <p>
 * Represents a composite step, which can be declared both programmatically, via the
 * {@link org.jbehave.core.annotations.Composite @Composite} annotation, or via a textual representation.
 * </p>
 *
 * @author Mauro Talevi
 * @author Valery Yatsynovich
 */
public class Composite {

    private StepType stepType;
    private String stepWithoutStartingWord;
    private List<String> steps;

    public Composite(StepType stepType, String stepWithoutStartingWord, List<String> steps) {
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
