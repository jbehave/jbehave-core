package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jbehave.core.model.StepPattern;

/**
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null Object Pattern</a> implementation of {@link StepMonitor}.
 * Can be extended to override only the methods of interest.
 */
public class NullStepMonitor implements StepMonitor {
    public void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType,
            Method method, Object stepsInstance) {
    }

    public void stepMatchesPattern(String step, boolean matches, StepPattern pattern, Method method,
            Object stepsInstance) {
    }

    public void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass) {
    }

    public void performing(String step, boolean dryRun) {
    }

    public void usingAnnotatedNameForParameter(String name, int position) {
    }

    public void usingParameterNameForParameter(String name, int position) {
    }

    public void usingTableAnnotatedNameForParameter(String name, int position) {
    }

    public void usingTableParameterNameForParameter(String name, int position) {
    }

    public void usingNaturalOrderForParameter(int position) {
    }

    public void foundParameter(String parameter, int position) {
    }
}