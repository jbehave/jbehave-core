package org.jbehave.core.steps;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.parsers.StepMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.jbehave.core.steps.AbstractStepResult.*;

public class StepCreator {

    public static final String PARAMETER_NAME_START = "<";
    public static final String PARAMETER_NAME_END = ">";
    public static final String PARAMETER_VALUE_START = "\uFF5F";
    public static final String PARAMETER_VALUE_END = "\uFF60";
    public static final String PARAMETER_VALUE_NEWLINE = "\u2424";
    public static final UUIDExceptionWrapper NO_FAILURE = new UUIDExceptionWrapper("no failure");
    private final Object stepsInstance;
    private final ParameterConverters parameterConverters;
    private final StepMatcher stepMatcher;
    private final StepRunner beforeOrAfter;
    private final StepRunner skip;
    private StepMonitor stepMonitor;
    private Paranamer paranamer = new NullParanamer();
    private boolean dryRun = false;

    public StepCreator(Object stepsInstance, StepMonitor stepMonitor) {
        this(stepsInstance, null, null, stepMonitor);
    }

    public StepCreator(Object stepsInstance, ParameterConverters parameterConverters, StepMatcher stepMatcher,
            StepMonitor stepMonitor) {
        this.stepsInstance = stepsInstance;
        this.parameterConverters = parameterConverters;
        this.stepMatcher = stepMatcher;
        this.stepMonitor = stepMonitor;
        this.beforeOrAfter = new BeforeOrAfter();
        this.skip = new Skip();
    }

    public void useStepMonitor(StepMonitor stepMonitor) {
        this.stepMonitor = stepMonitor;
    }

    public void useParanamer(Paranamer paranamer) {
        this.paranamer = paranamer;
    }

    public void doDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public Step createBeforeOrAfterStep(final Method method) {
        return new BeforeOrAfterStep(method);
    }

    public Step createAfterStepUponOutcome(final Method method, final Outcome outcome, final boolean failureOccured) {
        switch (outcome) {
        case ANY:
        default:
            return new AnyOrDefaultStep(method);
        case SUCCESS:
            return new SuccessStep(failureOccured, method);
        case FAILURE:
            return new FailureStep(failureOccured, method);
        }
    }

    public Map<String, String> matchedParameters(final Method method, final String stepAsString,
            final String stepWithoutStartingWord, final Map<String, String> namedParameters) {
        stepMatcher.find(stepWithoutStartingWord);
        String[] annotationNames = annotatedParameterNames(method);
        String[] parameterNames = paranamer.lookupParameterNames(method, false);
        Type[] types = method.getGenericParameterTypes();
        String[] names = (annotationNames.length > 0 ? annotationNames : parameterNames);
        String[] parameters = parametersForStep(namedParameters, types, annotationNames, parameterNames);
        Map<String, String> matchedParameters = new HashMap<String, String>();
        for (int i = 0; i < names.length; i++) {
            matchedParameters.put(names[i], parameters[i]);
        }
        return matchedParameters;
    }

    public Step createParametrisedStep(final Method method, final String stepAsString,
            final String stepWithoutStartingWord, final Map<String, String> namedParameters) {
        return new ParameterizedStep(stepAsString, method, stepWithoutStartingWord, namedParameters);
    }

    /**
     * Extract annotated parameter names from the @Named parameter annotations
     * of the method
     * 
     * @param method the Method containing the annotations
     * @return An array of annotated parameter names, which <b>may</b> include
     *         <code>null</code> values for parameters that are not annotated
     */
    private String[] annotatedParameterNames(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        String[] names = new String[parameterAnnotations.length];
        for (int x = 0; x < parameterAnnotations.length; x++) {
            Annotation[] annotations = parameterAnnotations[x];
            for (Annotation annotation : annotations) {
                names[x] = annotationName(annotation);
            }
        }
        return names;
    }

    private String annotationName(Annotation annotation) {
        if (annotation.annotationType().isAssignableFrom(Named.class)) {
            return ((Named) annotation).value();
        } else if ("javax.inject.Named".equals(annotation.annotationType().getName())) {
            return Jsr330Helper.getNamedValue(annotation);
        } else {
            return null;
        }
    }

    private String parametrisedStep(String stepAsString, Map<String, String> namedParameters, Type[] types,
            String[] annotationNames, String[] parameterNames, String[] parameters) {
        String parametrisedStep = stepAsString;
        for (int position = 0; position < types.length; position++) {
            parametrisedStep = replaceParameterValuesInStep(parametrisedStep, position, annotationNames,
                    parameterNames, parameters, namedParameters);
        }
        return parametrisedStep;
    }

    private String replaceParameterValuesInStep(String stepText, int position, String[] annotationNames,
            String[] parameterNames, String[] parameters, Map<String, String> namedParameters) {
        int annotatedNamePosition = parameterPosition(annotationNames, position);
        int parameterNamePosition = parameterPosition(parameterNames, position);
        if (annotatedNamePosition != -1) {
            stepText = replaceParameterValue(stepText, namedParameters, annotationNames[position]);
        } else if (parameterNamePosition != -1) {
            stepText = replaceParameterValue(stepText, namedParameters, parameterNames[position]);
        }
        stepText = replaceParameterValue(stepText, position, parameters);
        return stepText;
    }

    private String replaceParameterValue(String stepText, int position, String[] parameters) {
        String value = parameters[position];
        if (value != null) {
            stepText = stepText.replace(value, PARAMETER_VALUE_START + value + PARAMETER_VALUE_END);
            stepText = stepText.replace("\n", PARAMETER_VALUE_NEWLINE);
        }
        return stepText;
    }

    private String replaceParameterValue(String stepText, Map<String, String> namedParameters, String name) {
        String value = tableParameter(namedParameters, name);
        if (value != null) {
            stepText = stepText.replace(PARAMETER_NAME_START + name + PARAMETER_NAME_END, PARAMETER_VALUE_START + value
                    + PARAMETER_VALUE_END);
        }
        return stepText;
    }

    private String[] parametersForStep(Map<String, String> namedParameters, Type[] types, String[] annotationNames,
            String[] parameterNames) {
        final String[] parameters = new String[types.length];
        for (int position = 0; position < types.length; position++) {
            parameters[position] = parameterForPosition(position, annotationNames, parameterNames, namedParameters);
        }
        return parameters;
    }

    private Object[] convertParameters(String[] parametersAsString, Type[] types) {
        final Object[] parameters = new Object[parametersAsString.length];
        for (int position = 0; position < parametersAsString.length; position++) {
            parameters[position] = parameterConverters.convert(parametersAsString[position], types[position]);
        }
        return parameters;
    }

    private String parameterForPosition(int position, String[] annotationNames, String[] parameterNames,
            Map<String, String> namedParameters) {
        int annotatedNamePosition = parameterPosition(annotationNames, position);
        int parameterNamePosition = parameterPosition(parameterNames, position);
        String parameter = null;
        if (annotatedNamePosition != -1 && isGroupName(annotationNames[position])) {
            String name = annotationNames[position];
            stepMonitor.usingAnnotatedNameForParameter(name, position);
            parameter = matchedParameter(name);
        } else if (parameterNamePosition != -1 && isGroupName(parameterNames[position])) {
            String name = parameterNames[position];
            stepMonitor.usingParameterNameForParameter(name, position);
            parameter = matchedParameter(name);
        } else if (annotatedNamePosition != -1 && isTableName(namedParameters, annotationNames[position])) {
            String name = annotationNames[position];
            stepMonitor.usingTableAnnotatedNameForParameter(name, position);
            parameter = tableParameter(namedParameters, name);
        } else if (parameterNamePosition != -1 && isTableName(namedParameters, parameterNames[position])) {
            String name = parameterNames[position];
            stepMonitor.usingTableParameterNameForParameter(name, position);
            parameter = tableParameter(namedParameters, name);
        } else {
            stepMonitor.usingNaturalOrderForParameter(position);
            parameter = matchedParameter(position);
        }
        stepMonitor.foundParameter(parameter, position);
        return parameter;
    }

    String matchedParameter(String name) {
        String[] parameterNames = stepMatcher.parameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            if (name.equals(parameterName)) {
                return matchedParameter(i);
            }
        }
        throw new ParameterNotFound(name, parameterNames);
    }

    private String matchedParameter(int position) {
        String[] parameterNames = stepMatcher.parameterNames();
        int matchedPosition = position + 1;
        if (matchedPosition <= parameterNames.length) {
            return stepMatcher.parameter(matchedPosition);
        }
        throw new ParameterNotFound(position, parameterNames);
    }

    private int parameterPosition(String[] names, int position) {
        if (names.length == 0) {
            return -1;
        }
        String positionName = names[position];
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (name != null && positionName.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isGroupName(String name) {
        String[] groupNames = stepMatcher.parameterNames();
        for (String groupName : groupNames) {
            if (name.equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    private String tableParameter(Map<String, String> namedParameters, String name) {
        return namedParameters.get(name);
    }

    private boolean isTableName(Map<String, String> namedParameters, String name) {
        return tableParameter(namedParameters, name) != null;
    }

    public interface StepRunner {

        StepResult run(Method method, UUIDExceptionWrapper failureIfItHappened);

    }

    private class BeforeOrAfter implements StepRunner {

        public StepResult run(Method method, UUIDExceptionWrapper failureIfItHappened) {
            if (method == null) {
                return failed(method, new UUIDExceptionWrapper(new BeforeOrAfterFailed(new NullPointerException(
                        "method"))));
            }
            try {
                if (method.getParameterTypes().length == 0) {
                    method.invoke(stepsInstance);
                } else {
                    method.invoke(stepsInstance, failureIfItHappened);
                }
            } catch (InvocationTargetException e) {
                return failed(method, new UUIDExceptionWrapper(new BeforeOrAfterFailed(method, e.getCause())));
            } catch (Throwable t) {
                return failed(method, new UUIDExceptionWrapper(new BeforeOrAfterFailed(method, t)));
            }
            return skipped();
        }
    }

    private class Skip implements StepRunner {

        public StepResult run(Method method, UUIDExceptionWrapper failureIfItHappened) {
            return skipped();
        }
    }

    public static Step createPendingStep(final String stepAsString, String previousNonAndStep) {
        return new PendingStep(stepAsString, previousNonAndStep);
    }

    public static Step createIgnorableStep(final String stepAsString) {
        return new IgnorableStep(stepAsString);
    }

    /**
     * This is a different class, because the @Inject jar may not be in the
     * classpath.
     */
    public static class Jsr330Helper {

        private static String getNamedValue(Annotation annotation) {
            return ((javax.inject.Named) annotation).value();
        }

    }

    @SuppressWarnings("serial")
    public static class ParameterNotFound extends RuntimeException {

        public ParameterNotFound(String name, String[] parameters) {
            super("Parameter not found for name '" + name + "' amongst '" + asList(parameters) + "'");
        }

        public ParameterNotFound(int position, String[] parameters) {
            super("Parameter not found for position '" + position + "' amongst '" + asList(parameters) + "'");
        }
    }

    public static abstract class AbstractStep implements Step {

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }

    }

    public class BeforeOrAfterStep extends AbstractStep {
        private final Method method;

        public BeforeOrAfterStep(Method method) {
            this.method = method;
        }

        public StepResult doNotPerform() {
            return skip.run(method, NO_FAILURE);
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return beforeOrAfter.run(method, NO_FAILURE);
        }

    }

    public class AnyOrDefaultStep extends AbstractStep {

        private final Method method;

        public AnyOrDefaultStep(Method method) {
            this.method = method;
        }

        public StepResult doNotPerform() {
            return beforeOrAfter.run(method, NO_FAILURE);
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return beforeOrAfter.run(method, NO_FAILURE);
        }

    }

    public class SuccessStep extends AbstractStep {

        private final boolean failureOccured;
        private final Method method;

        public SuccessStep(boolean failureOccured, Method method) {
            this.failureOccured = failureOccured;
            this.method = method;
        }

        public StepResult doNotPerform() {
            return (failureOccured ? skip.run(method, NO_FAILURE) : beforeOrAfter.run(method, NO_FAILURE));
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return (failureOccured ? skip.run(method, NO_FAILURE) : beforeOrAfter.run(method, NO_FAILURE));
        }

    }

    public class FailureStep extends AbstractStep {

        private final boolean failureOccured;
        private final Method method;

        public FailureStep(boolean failureOccured, Method method) {
            this.failureOccured = failureOccured;
            this.method = method;
        }

        public StepResult doNotPerform() {
            return (failureOccured ? beforeOrAfter.run(method, NO_FAILURE) : skip.run(method, NO_FAILURE));
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return (failureOccured ? beforeOrAfter.run(method, storyFailureIfItHappened) : skip.run(method, NO_FAILURE));
        }

    }

    public class ParameterizedStep extends AbstractStep {
        private Object[] convertedParameters;
        private String parametrisedStep;
        private final String stepAsString;
        private final Method method;
        private final String stepWithoutStartingWord;
        private final Map<String, String> namedParameters;

        public ParameterizedStep(String stepAsString, Method method, String stepWithoutStartingWord,
                Map<String, String> namedParameters) {
            this.stepAsString = stepAsString;
            this.method = method;
            this.stepWithoutStartingWord = stepWithoutStartingWord;
            this.namedParameters = namedParameters;
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            try {
                parametriseStep();
                stepMonitor.performing(stepAsString, dryRun);
                if (!dryRun) {
                    method.invoke(stepsInstance, convertedParameters);
                }
                return successful(stepAsString).withParameterValues(parametrisedStep);
            } catch (ParameterNotFound e) {
                // step parametrisation failed, return pending StepResult
                return pending(stepAsString).withParameterValues(parametrisedStep);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof UUIDExceptionWrapper) {
                    return failed(stepAsString, ((UUIDExceptionWrapper) e.getCause())).withParameterValues(
                            parametrisedStep);
                }
                return failed(stepAsString, new UUIDExceptionWrapper(e.getCause())).withParameterValues(
                        parametrisedStep);
            } catch (Throwable t) {
                return failed(stepAsString, new UUIDExceptionWrapper(t)).withParameterValues(parametrisedStep);
            }
        }

        public StepResult doNotPerform() {
            try {
                parametriseStep();
                // } catch (ParameterNotFound e) {
            } catch (Throwable t) {
                // step parametrisation failed, but still return
                // notPerformed StepResult
            }
            return notPerformed(stepAsString).withParameterValues(parametrisedStep);
        }

        private void parametriseStep() {
            stepMatcher.find(stepWithoutStartingWord);
            String[] annotationNames = annotatedParameterNames(method);
            String[] parameterNames = paranamer.lookupParameterNames(method, false);
            Type[] types = method.getGenericParameterTypes();
            String[] parameters = parametersForStep(namedParameters, types, annotationNames, parameterNames);
            convertedParameters = convertParameters(parameters, types);
            parametrisedStep = parametrisedStep(stepAsString, namedParameters, types, annotationNames, parameterNames,
                    parameters);
        }

    }

    public static class PendingStep extends AbstractStep {
        private final String stepAsString;
        private final String previousNonAndStep;
        private Method method;

        public PendingStep(String stepAsString, String previousNonAndStep) {
            this.stepAsString = stepAsString;
            this.previousNonAndStep = previousNonAndStep;
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return pending(stepAsString);
        }

        public StepResult doNotPerform() {
            return pending(stepAsString);
        }

        public String stepAsString() {
            return stepAsString;
        }

        public String previousNonAndStepAsString() {
            return previousNonAndStep;
        }

        public void annotatedOn(Method method) {
            this.method = method;
        }

        public boolean annotated() {
            return method != null;
        }

    }

    public static class IgnorableStep extends AbstractStep {
        private final String stepAsString;

        public IgnorableStep(String stepAsString) {
            this.stepAsString = stepAsString;
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return ignorable(stepAsString);
        }

        public StepResult doNotPerform() {
            return ignorable(stepAsString);
        }
    }

}
