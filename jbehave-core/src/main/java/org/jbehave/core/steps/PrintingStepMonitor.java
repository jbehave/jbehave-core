package org.jbehave.core.steps;

import org.jbehave.core.model.StepPattern;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Abstract {@code StepMonitor} that prints to output which should be defined in child implementations.
 */
public abstract class PrintingStepMonitor implements StepMonitor {

    private static final String CONVERTED_VALUE_OF_TYPE = "Converted value '%s' of type '%s' to '%s' with %s '%s'";
    private static final String STEP_MATCHES_TYPE = "Step '%s' (with previous step '%s') %s type '%s' for method '%s' with annotations '%s' in steps instance '%s'";
    private static final String STEP_MATCHES_PATTERN = "Step '%s' %s pattern '%s' for method '%s' with annotations '%s' in steps instance '%s'";
    private static final String PERFORMING = "Performing step '%s'%s";
    private static final String DRY_RUN = " (DRY RUN)";
    private static final String MATCHES = "matches";
    private static final String DOES_NOT_MATCH = "does not match";
    private static final String USING_NAME_FOR_PARAMETER = "Using %s name '%s' for parameter position %d";
    private static final String ANNOTATED = "annotated";
    private static final String PARAMETER = "parameter";
    private static final String TABLE_ANNOTATED = "table annotated";
    private static final String TABLE_PARAMETER = "table parameter";
    private static final String USING_NATURAL_ORDER_FOR_PARAMETER = "Using natural order for parameter position %d";
    private static final String FOUND_PARAMETER = "Found parameter '%s' for position %d";
    private static final String STEPS_CONTEXT_PARAMETER = "Found parameter '%s' from Steps Context";

    @Override
    public void stepMatchesType(String step, String previous, boolean matches, StepType stepType, Method method,
            Object stepsInstance) {
        print(STEP_MATCHES_TYPE, step, previous, matches(matches), stepType, method, getAnnotations(method),
                stepsInstance);
    }

    @Override
    public void stepMatchesPattern(String step, boolean matches, StepPattern stepPattern, Method method,
            Object stepsInstance) {
        print(STEP_MATCHES_PATTERN, step, matches(matches), stepPattern, method, getAnnotations(method), stepsInstance);
    }

    @Override
    public void convertedValueOfType(String value, Type type, Object converted, Queue<Class<?>> converterClasses) {
        String classes = converterClasses.stream().map(Class::getName).collect(Collectors.joining(" -> "));
        print(CONVERTED_VALUE_OF_TYPE, value, type, converted, "converters", classes);
    }

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method) {
        print(PERFORMING, step, dryRun ? DRY_RUN : "");
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method) {
    }

    @Override
    public void usingAnnotatedNameForParameter(String name, int position) {
        print(USING_NAME_FOR_PARAMETER, ANNOTATED, name, position);
    }

    @Override
    public void usingParameterNameForParameter(String name, int position) {
        print(USING_NAME_FOR_PARAMETER, PARAMETER, name, position);
    }

    @Override
    public void usingTableAnnotatedNameForParameter(String name, int position) {
        print(USING_NAME_FOR_PARAMETER, TABLE_ANNOTATED, name, position);
    }

    @Override
    public void usingTableParameterNameForParameter(String name, int position) {
        print(USING_NAME_FOR_PARAMETER, TABLE_PARAMETER, name, position);
    }

    @Override
    public void usingNaturalOrderForParameter(int position) {
        print(USING_NATURAL_ORDER_FOR_PARAMETER, position);
    }

    @Override
    public void foundParameter(String parameter, int position) {
        print(FOUND_PARAMETER, parameter, position);
    }

    @Override
    public void usingStepsContextParameter(String parameter) {
        print(STEPS_CONTEXT_PARAMETER, parameter);
    }

    protected abstract void print(String format, Object... args);

    private List<Annotation> getAnnotations(Method method) {
        return method != null ? asList(method.getAnnotations()) : Collections.<Annotation>emptyList();
    }

    private String matches(boolean matches) {
        return matches ? MATCHES : DOES_NOT_MATCH;
    }
}
