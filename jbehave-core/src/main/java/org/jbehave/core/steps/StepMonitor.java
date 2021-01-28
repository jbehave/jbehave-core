package org.jbehave.core.steps;

import org.jbehave.core.model.StepPattern;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Queue;

/**
 * Interface to monitor step events
 */
public interface StepMonitor {

    void stepMatchesType(String stepAsString, String previousAsString, boolean matchesType, StepType stepType,
            Method method, Object stepsInstance);

    void stepMatchesPattern(String step, boolean matches, StepPattern stepPattern, Method method, Object stepsInstance);

    void convertedValueOfType(String value, Type type, Object converted, Queue<Class<?>> converterClasses);

    void beforePerforming(String step, boolean dryRun, Method method);

    void afterPerforming(String step, boolean dryRun, Method method);

    void usingAnnotatedNameForParameter(String name, int position);

    void usingParameterNameForParameter(String name, int position);

    void usingTableAnnotatedNameForParameter(String name, int position);

    void usingTableParameterNameForParameter(String name, int position);

    void usingNaturalOrderForParameter(int position);

    void foundParameter(String parameter, int position);

    void usingStepsContextParameter(String parameter);
}
