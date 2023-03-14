package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Queue;

import org.jbehave.core.model.StepPattern;

/**
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null Object Pattern</a> implementation of {@link StepMonitor}.
 * Can be extended to override only the methods of interest.
 */
public class NullStepMonitor implements StepMonitor {
    @Override
    public void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType,
            Method method, Object stepsInstance) {
        // Do nothing by default
    }

    @Override
    public void stepMatchesPattern(String step, boolean matches, StepPattern pattern, Method method,
            Object stepsInstance) {
        // Do nothing by default
    }

    @Override
    public void convertedValueOfType(String value, Type type, Object converted, Queue<Class<?>> converterClasses) {
        // Do nothing by default
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method) {
        // Do nothing by default
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method) {
        // Do nothing by default
    }

    @Override
    public void usingAnnotatedNameForParameter(String name, int position) {
        // Do nothing by default
    }

    @Override
    public void usingParameterNameForParameter(String name, int position) {
        // Do nothing by default
    }

    @Override
    public void usingTableAnnotatedNameForParameter(String name, int position) {
        // Do nothing by default
    }

    @Override
    public void usingTableParameterNameForParameter(String name, int position) {
        // Do nothing by default
    }

    @Override
    public void usingNaturalOrderForParameter(int position) {
        // Do nothing by default
    }

    @Override
    public void foundParameter(String parameter, int position) {
        // Do nothing by default
    }

    @Override
    public void usingStepsContextParameter(String parameter) {
        // Do nothing by default
    }
}
