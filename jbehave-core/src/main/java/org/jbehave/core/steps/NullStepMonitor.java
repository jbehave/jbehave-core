package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jbehave.core.model.StepPattern;

/**
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null Object Pattern</a> implementation of {@link StepMonitor}.
 * Can be extended to override only the methods of interest.
 */
public class NullStepMonitor implements StepMonitor {
    @Override
    public void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType,
            Method method, Object stepsInstance) {
    }

    @Override
    public void stepMatchesPattern(String step, boolean matches, StepPattern pattern, Method method,
            Object stepsInstance) {
    }

    @Override
    public void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass) {
    }

    @Override
    @Deprecated
    public void performing(String step, boolean dryRun) {
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method) {
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method) {
    }

    @Override
    public void usingAnnotatedNameForParameter(String name, int position) {
    }

    @Override
    public void usingParameterNameForParameter(String name, int position) {
    }

    @Override
    public void usingTableAnnotatedNameForParameter(String name, int position) {
    }

    @Override
    public void usingTableParameterNameForParameter(String name, int position) {
    }

    @Override
    public void usingNaturalOrderForParameter(int position) {
    }

    @Override
    public void foundParameter(String parameter, int position) {
    }

    @Override
    public void usingStepsContextParameter(String parameter) {
    }
}
