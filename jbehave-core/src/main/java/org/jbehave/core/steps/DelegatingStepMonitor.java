package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jbehave.core.model.StepPattern;

public class DelegatingStepMonitor implements StepMonitor {

    protected final StepMonitor delegate;

    public DelegatingStepMonitor(StepMonitor delegate) {
        this.delegate = delegate;
    }

    public void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass) {
    	delegate.convertedValueOfType(value, type, converted, converterClass);
    }

    public void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType, Method method, Object stepsInstance) {
    	delegate.stepMatchesType(stepAsString, previousAsString, matchesType, stepType, method, stepsInstance);
    }

    public void stepMatchesPattern(String step, boolean matches, StepPattern stepPattern, Method method, Object stepsInstance) {
    	delegate.stepMatchesPattern(step, matches, stepPattern, method, stepsInstance);
    }

    public void foundParameter(String parameter, int position) {
    	delegate.foundParameter(parameter, position);
    }

    public void performing(String step, boolean dryRun) {
        delegate.performing(step, dryRun);
    }

    public void usingAnnotatedNameForParameter(String name, int position) {
    	delegate.usingAnnotatedNameForParameter(name, position);
    }

    public void usingNaturalOrderForParameter(int position) {
    	delegate.usingNaturalOrderForParameter(position);
    }

    public void usingParameterNameForParameter(String name, int position) {
    	delegate.usingParameterNameForParameter(name, position);
    }

    public void usingTableAnnotatedNameForParameter(String name, int position) {
    	delegate.usingTableAnnotatedNameForParameter(name, position);
    }

    public void usingTableParameterNameForParameter(String name, int position) {
    	delegate.usingTableParameterNameForParameter(name, position);
    }

}