package org.jbehave.core.steps;

import java.lang.reflect.Method;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A Stepdoc represents the documentation on a single {@link StepCandidate},
 * which includes:
 * <ul>
 * <li>the step type</li>
 * <li>the pattern to match the step candidate that is configured in the
 * annotation</li>
 * <li>the method in the steps instance class</li>
 * <li>the steps instance class</li>
 * </ul>
 */
public class Stepdoc implements Comparable<Stepdoc> {

    private StepType stepType;
    private String startingWord;
    private String pattern;
    private Method method;
    private Object stepsInstance;

    public Stepdoc(StepCandidate candidate) {
        this.method = candidate.getMethod();
        this.stepType = candidate.getStepType();
        this.startingWord = candidate.getStartingWord();
        this.pattern = candidate.getPatternAsString();
        this.stepsInstance = candidate.getStepsInstance();
    }

    public StepType getStepType() {
        return stepType;
    }

    public String getStartingWord() {
        return startingWord;
    }

    public String getPattern() {
        return pattern;
    }

    public Method getMethod() {
        return method;
    }

    public Object getStepsInstance() {
        return stepsInstance;
    }

    /**
     * Method signature without "public void" prefix
     * 
     * @return The method signature in String format
     */
    public String getMethodSignature() {
        if (method != null) {
            String methodSignature = method.toString();
            return methodSignature.replaceFirst("public void ", "");
        }
        return null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }

    public int compareTo(Stepdoc that) {
        return CompareToBuilder.reflectionCompare(this, that);
    }

}
