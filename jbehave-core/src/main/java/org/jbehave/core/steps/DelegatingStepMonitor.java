package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jbehave.core.model.StepPattern;

public class DelegatingStepMonitor implements StepMonitor {

    protected final StepMonitor delegate;

    public DelegatingStepMonitor(StepMonitor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass) {
    	delegate.convertedValueOfType(value, type, converted, converterClass);
    }

    @Override
    public void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType, Method method, Object stepsInstance) {
    	delegate.stepMatchesType(stepAsString, previousAsString, matchesType, stepType, method, stepsInstance);
    }

    @Override
    public void stepMatchesPattern(String step, boolean matches, StepPattern stepPattern, Method method, Object stepsInstance) {
    	delegate.stepMatchesPattern(step, matches, stepPattern, method, stepsInstance);
    }

    @Override
    public void foundParameter(String parameter, int position) {
    	delegate.foundParameter(parameter, position);
    }

    @Override
    public void performing(String step, boolean dryRun) {
        delegate.performing(step, dryRun);
    }

    @Override
    public void usingAnnotatedNameForParameter(String name, int position) {
    	delegate.usingAnnotatedNameForParameter(name, position);
    }

    @Override
    public void usingNaturalOrderForParameter(int position) {
    	delegate.usingNaturalOrderForParameter(position);
    }

    @Override
    public void usingParameterNameForParameter(String name, int position) {
    	delegate.usingParameterNameForParameter(name, position);
    }

    @Override
    public void usingTableAnnotatedNameForParameter(String name, int position) {
    	delegate.usingTableAnnotatedNameForParameter(name, position);
    }

    @Override
    public void usingTableParameterNameForParameter(String name, int position) {
    	delegate.usingTableParameterNameForParameter(name, position);
    }

    @Override
    public void usingStepsContextParameter(String parameter) {
        delegate.usingStepsContextParameter(parameter);
    }

}
