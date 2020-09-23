package org.jbehave.core.model;

import java.util.List;

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
public class Composite extends StepsContainer {

    private StepType stepType;
    private String stepWithoutStartingWord;
    private int priority;

    public Composite(StepType stepType, String stepWithoutStartingWord, int priority, List<String> steps) {
        super(steps);
        this.stepType = stepType;
        this.stepWithoutStartingWord = stepWithoutStartingWord;
        this.priority = priority;
    }

    public StepType getStepType() {
        return stepType;
    }

    public String getStepWithoutStartingWord() {
        return stepWithoutStartingWord;
    }

    public int getPriority() {
        return priority;
    }
}
