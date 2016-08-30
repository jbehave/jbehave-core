package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jbehave.core.model.StepPattern;

/**
 * Interface to monitor step events
 */
public interface StepMonitor {

    void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType,
            Method method, Object stepsInstance);

    void stepMatchesPattern(String step, boolean matches, StepPattern stepPattern, Method method, Object stepsInstance);

    void convertedValueOfType(String value, Type type, Object converted, Class<?> converterClass);

    void performing(String step, boolean dryRun);

    void usingAnnotatedNameForParameter(String name, int position);

    void usingParameterNameForParameter(String name, int position);

    void usingTableAnnotatedNameForParameter(String name, int position);

    void usingTableParameterNameForParameter(String name, int position);

    void usingNaturalOrderForParameter(int position);

    void foundParameter(String parameter, int position);

    void usingStepsContextParameter(String parameter);
}
