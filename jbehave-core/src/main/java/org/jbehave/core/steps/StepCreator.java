package org.jbehave.core.steps;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.IgnoringStepsFailure;
import org.jbehave.core.failures.RestartingScenarioFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Meta;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.reporters.StoryReporter;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

import static java.util.Arrays.asList;
import static org.jbehave.core.steps.AbstractStepResult.failed;
import static org.jbehave.core.steps.AbstractStepResult.ignorable;
import static org.jbehave.core.steps.AbstractStepResult.notPerformed;
import static org.jbehave.core.steps.AbstractStepResult.pending;
import static org.jbehave.core.steps.AbstractStepResult.silent;
import static org.jbehave.core.steps.AbstractStepResult.skipped;
import static org.jbehave.core.steps.AbstractStepResult.successful;

public class StepCreator {

	public static final String PARAMETER_TABLE_START = "\uff3b";
    public static final String PARAMETER_TABLE_END = "\uff3d";
    public static final String PARAMETER_VALUE_START = "\uFF5F";
    public static final String PARAMETER_VALUE_END = "\uFF60";
    public static final String PARAMETER_VALUE_NEWLINE = "\u2424";
    public static final UUIDExceptionWrapper NO_FAILURE = new UUIDExceptionWrapper("no failure");
	private static final String NEWLINE = "\n";
    private static final String SPACE = " ";
	private static final String NONE = "";
    private final Class<?> stepsType;
    private final InjectableStepsFactory stepsFactory;
    private final ParameterConverters parameterConverters;
    private final ParameterControls parameterControls;
    private final Pattern delimitedNamePattern;
    private final StepMatcher stepMatcher;
    private StepMonitor stepMonitor;
    private Paranamer paranamer = new NullParanamer();
    private boolean dryRun = false;

    public StepCreator(Class<?> stepsType, InjectableStepsFactory stepsFactory,
            ParameterConverters parameterConverters, ParameterControls parameterControls, StepMatcher stepMatcher,
            StepMonitor stepMonitor) {
        this.stepsType = stepsType;
        this.stepsFactory = stepsFactory;
        this.parameterConverters = parameterConverters;
        this.parameterControls = parameterControls;
        this.stepMatcher = stepMatcher;
        this.stepMonitor = stepMonitor;
        this.delimitedNamePattern = Pattern.compile(parameterControls.nameDelimiterLeft() + "(\\w+?)"
                + parameterControls.nameDelimiterRight());
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

    public Object stepsInstance() {
        return stepsFactory.createInstanceOfType(stepsType);
    }

    public Step createBeforeOrAfterStep(Method method, Meta meta) {
        return new BeforeOrAfterStep(method, meta);
    }

    public Step createAfterStepUponOutcome(final Method method, final Outcome outcome, Meta storyAndScenarioMeta) {
        switch (outcome) {
        case ANY:
        default:
            return new BeforeOrAfterStep(method, storyAndScenarioMeta);
        case SUCCESS:
            return new UponSuccessStep(method, storyAndScenarioMeta);
        case FAILURE:
            return new UponFailureStep(method, storyAndScenarioMeta);
        }
    }

    public Map<String, String> matchedParameters(final Method method, final String stepAsString,
            final String stepWithoutStartingWord, final Map<String, String> namedParameters) {
        Map<String, String> matchedParameters = new HashMap<String, String>(); 
        if (stepMatcher.find(stepWithoutStartingWord)) { 
            // we've found a match, populate map
            ParameterName[] parameterNames = parameterNames(method);
            Type[] types = method.getGenericParameterTypes();
            String[] values = parameterValuesForStep(namedParameters, types, parameterNames);
    
            
            for (int i = 0; i < parameterNames.length; i++) {
                String name = parameterNames[i].name;
                if (name == null) {
                    name = stepMatcher.parameterNames()[i];
                }
                matchedParameters.put(name, values[i]);
            }
        }
        // else return empty map
        return matchedParameters; 
    }

    /**
     * Returns the {@link ParameterName} representations for the method,
     * providing an abstraction that supports both annotated and non-annotated
     * parameters.
     * 
     * @param method the Method
     * @return The array of {@link ParameterName}s
     */
    private ParameterName[] parameterNames(Method method) {
        String[] annotatedNames = annotatedParameterNames(method);
        String[] paranamerNames = paranamerParameterNames(method);

        ParameterName[] parameterNames = new ParameterName[annotatedNames.length];
        for (int i = 0; i < annotatedNames.length; i++) {
            parameterNames[i] = parameterName(annotatedNames, paranamerNames, i);
        }
        return parameterNames;
    }

    private ParameterName parameterName(String[] annotatedNames, String[] paranamerNames, int i) {
        String name = annotatedNames[i];
        boolean annotated = true;
        if (name == null) {
            name = (paranamerNames.length > i ? paranamerNames[i] : null);
            annotated = false;
        }
        return new ParameterName(name, annotated);
    }

    /**
     * Extract parameter names using {@link Named}-annotated parameters
     * 
     * @param method the Method with {@link Named}-annotated parameters
     * @return An array of annotated parameter names, which <b>may</b> include
     *         <code>null</code> values for parameters that are not annotated
     */
    private String[] annotatedParameterNames(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        String[] names = new String[parameterAnnotations.length];
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                names[i] = annotationName(annotation);
            }
        }
        return names;
    }

    /**
     * Returns either the value of the annotation, either {@link Named} or
     * "javax.inject.Named".
     * 
     * @param annotation the Annotation
     * @return The annotated value or <code>null</code> if no annotation is
     *         found
     */
    private String annotationName(Annotation annotation) {
        if (annotation.annotationType().isAssignableFrom(Named.class)) {
            return ((Named) annotation).value();
        } else if ("javax.inject.Named".equals(annotation.annotationType().getName())) {
            return Jsr330Helper.getNamedValue(annotation);
        } else {
            return null;
        }
    }

    /**
     * Extract parameter names using
     * {@link Paranamer#lookupParameterNames(AccessibleObject, boolean)}
     * 
     * @param method the Method inspected by Paranamer
     * @return An array of parameter names looked up by Paranamer
     */
    private String[] paranamerParameterNames(Method method) {
        return paranamer.lookupParameterNames(method, false);
    }

    public Step createParametrisedStep(final Method method, final String stepAsString,
            final String stepWithoutStartingWord, final Map<String, String> namedParameters) {
        return new ParametrisedStep(stepAsString, method, stepWithoutStartingWord, namedParameters);
    }

    public Step createParametrisedStepUponOutcome(final Method method, final String stepAsString,
            final String stepWithoutStartingWord, final Map<String, String> namedParameters, Outcome outcome) {
        switch (outcome) {
        case ANY:
            return new UponAnyParametrisedStep(stepAsString, method, stepWithoutStartingWord, namedParameters);
        case SUCCESS:
            return new UponSuccessParametrisedStep(stepAsString, method, stepWithoutStartingWord, namedParameters);
        case FAILURE:
            return new UponFailureParametrisedStep(stepAsString, method, stepWithoutStartingWord, namedParameters);
        default:
            return new ParametrisedStep(stepAsString, method, stepWithoutStartingWord, namedParameters);
        }
    }

    private String parametrisedStep(String stepAsString, Map<String, String> namedParameters, Type[] types,
            ParameterName[] names, String[] parameterValues) {
    	String parametrisedStep = stepAsString;
    	// mark parameter values that are parsed
    	boolean hasTable = hasTable(types);
        for (int position = 0; position < types.length; position++) {
            parametrisedStep = markParsedParameterValue(parametrisedStep, types[position], parameterValues[position], hasTable);
        }
        // mark parameter values that are named
        for (String name : namedParameters.keySet()) {
            parametrisedStep = markNamedParameterValue(parametrisedStep, namedParameters, name);
        }

        return parametrisedStep;
    }

    private boolean hasTable(Type[] types) {
    	for (Type type : types) {
			if ( isTable(type) ){
				return true;
			}
		}
		return false;
	}

	private String markNamedParameterValue(String stepText, Map<String, String> namedParameters, String name) {
        String value = namedParameter(namedParameters, name);
        if (value != null) {
			stepText = stepText.replace(delimitedName(name), markedValue(value));
        }
        return stepText;
    }

	private String delimitedName(String name) {
		return parameterControls.nameDelimiterLeft() + name + parameterControls.nameDelimiterRight();
	}

    private String markParsedParameterValue(String stepText, Type type, String value, boolean hasTable) {
        if (value != null) {
            if (isTable(type)) {
                stepText = stepText.replace(value, markedTable(value));
            } else {
                // only mark non-empty string as parameter (JBEHAVE-656)            	
                if (value.trim().length() != 0) {
                	String markedValue = markedValue(value);
                	// identify parameter values to mark as padded by spaces to avoid duplicated replacements of overlapping values (JBEHAVE-837)
                	String leftPad = SPACE;
                	String rightPad = ( stepText.endsWith(value) ? NONE : SPACE );
            		stepText = stepText.replace(pad(value, leftPad, rightPad), pad(markedValue, leftPad, rightPad));
                }
                if ( !hasTable ){
                	stepText = stepText.replace(NEWLINE, PARAMETER_VALUE_NEWLINE);
                }
            }
        }
        return stepText;
    }

	private String markedTable(String value) {
		return pad(value, PARAMETER_TABLE_START, PARAMETER_TABLE_END);
	}

	private String markedValue(String value) {
		return pad(value, PARAMETER_VALUE_START, PARAMETER_VALUE_END);
	}

	private String pad(String value, String left, String right){
		return new StringBuilder().append(left).append(value).append(right).toString();
	}
	
    private boolean isTable(Type type) {
        return type instanceof Class && ((Class<?>) type).isAssignableFrom(ExamplesTable.class);
    }

    private String[] parameterValuesForStep(Map<String, String> namedParameters, Type[] types, ParameterName[] names) {
        final String[] parameters = new String[types.length];
        for (int position = 0; position < types.length; position++) {
        	parameters[position] = parameterForPosition(position, names, namedParameters);
        }
        return parameters;
    }

    private Object[] convertParameterValues(String[] valuesAsString, Type[] types) {
        final Object[] parameters = new Object[valuesAsString.length];
        for (int position = 0; position < valuesAsString.length; position++) {
            parameters[position] = parameterConverters.convert(valuesAsString[position], types[position]);
        }
        return parameters;
    }

    private String parameterForPosition(int position, ParameterName[] names, Map<String, String> namedParameters) {
        int namePosition = parameterPosition(names, position);
        String parameter = null;

        if (namePosition != -1) {
            String name = names[position].name;
            boolean annotated = names[position].annotated;

            boolean delimitedNamedParameters = false;

            if (isGroupName(name)) {
                parameter = matchedParameter(name);
                String delimitedName = delimitedNameFor(parameter);

                if (delimitedName != null) {
                    name = delimitedName;
                    delimitedNamedParameters = true;
                } else {
                    monitorUsingNameForParameter(name, position, annotated);
                }
            }

            if (delimitedNamedParameters || isTableName(namedParameters, name)) {
                monitorUsingTableNameForParameter(name, position, annotated);
                parameter = namedParameter(namedParameters, name);
            }

        }

        if (parameter == null) {
            stepMonitor.usingNaturalOrderForParameter(position);
            parameter = matchedParameter(position);
            String delimitedName = delimitedNameFor(parameter);

            if (delimitedName != null && isTableName(namedParameters, delimitedName)) {
                parameter = namedParameter(namedParameters, delimitedName);
            }
        }

        stepMonitor.foundParameter(parameter, position);

        return parameter;
    }

    private void monitorUsingTableNameForParameter(String name, int position, boolean usingAnnotationNames) {
        if (usingAnnotationNames) {
            stepMonitor.usingTableAnnotatedNameForParameter(name, position);
        } else {
            stepMonitor.usingTableParameterNameForParameter(name, position);
        }
    }

    private void monitorUsingNameForParameter(String name, int position, boolean usingAnnotationNames) {
        if (usingAnnotationNames) {
            stepMonitor.usingAnnotatedNameForParameter(name, position);
        } else {
            stepMonitor.usingParameterNameForParameter(name, position);
        }
    }

    private String delimitedNameFor(String parameter) {
        if (!parameterControls.delimiterNamedParameters()) {
            return null;
        }
        Matcher matcher = delimitedNamePattern.matcher(parameter);
        return matcher.matches() ? matcher.group(1) : null;
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

    private int parameterPosition(ParameterName[] names, int position) {
        if (names.length == 0) {
            return -1;
        }
        String positionName = names[position].name;
        for (int i = 0; i < names.length; i++) {
            String name = names[i].name;
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

    private String namedParameter(Map<String, String> namedParameters, String name) {
        return namedParameters.get(name);
    }

    private boolean isTableName(Map<String, String> namedParameters, String name) {
        return namedParameter(namedParameters, name) != null;
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

    	public String asString(Keywords keywords) {
			return toString();
		}
    	
        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }

    }

    private class BeforeOrAfterStep extends AbstractStep {
        private final Method method;
        private final Meta meta;

        public BeforeOrAfterStep(Method method, Meta meta) {
            this.method = method;
            this.meta = meta;
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            ParameterConverters paramConvertersWithExceptionInjector = paramConvertersWithExceptionInjector(storyFailureIfItHappened);
            MethodInvoker methodInvoker = new MethodInvoker(method, paramConvertersWithExceptionInjector, paranamer,
                    meta);
            Timer timer = new Timer().start();
            try {
                methodInvoker.invoke();
                return silent(method).setTimings(timer.stop());
            } catch (InvocationTargetException e) {
                return failed(method, new UUIDExceptionWrapper(new BeforeOrAfterFailed(method, e.getCause())))
                        .setTimings(timer.stop());
            } catch (Throwable t) {
                return failed(method, new UUIDExceptionWrapper(new BeforeOrAfterFailed(method, t)))
                        .setTimings(timer.stop());
            }
        }

        private ParameterConverters paramConvertersWithExceptionInjector(UUIDExceptionWrapper storyFailureIfItHappened) {
            return parameterConverters.newInstanceAdding(new UUIDExceptionWrapperInjector(storyFailureIfItHappened));
        }

        public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return perform(storyFailureIfItHappened);
        }

        private class UUIDExceptionWrapperInjector implements ParameterConverters.ParameterConverter {
            private final UUIDExceptionWrapper storyFailureIfItHappened;

            public UUIDExceptionWrapperInjector(UUIDExceptionWrapper storyFailureIfItHappened) {
                this.storyFailureIfItHappened = storyFailureIfItHappened;
            }

            public boolean accept(Type type) {
                return UUIDExceptionWrapper.class == type;
            }

            public Object convertValue(String value, Type type) {
                return storyFailureIfItHappened;
            }
        }

		public String asString(Keywords keywords) {
			return method.getName()+";"+meta.asString(keywords);
		}
    }

    public class UponSuccessStep extends AbstractStep {
        private BeforeOrAfterStep beforeOrAfterStep;

        public UponSuccessStep(Method method, Meta storyAndScenarioMeta) {
            this.beforeOrAfterStep = new BeforeOrAfterStep(method, storyAndScenarioMeta);
        }

        public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return skipped();
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return beforeOrAfterStep.perform(storyFailureIfItHappened);
        }

		public String asString(Keywords keywords) {
			return beforeOrAfterStep.asString(keywords);
		}

    }

    public class UponFailureStep extends AbstractStep {
        private final BeforeOrAfterStep beforeOrAfterStep;

        public UponFailureStep(Method method, Meta storyAndScenarioMeta) {
            this.beforeOrAfterStep = new BeforeOrAfterStep(method, storyAndScenarioMeta);
        }

        public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return beforeOrAfterStep.perform(storyFailureIfItHappened);
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return skipped();
        }

		public String asString(Keywords keywords) {
			return beforeOrAfterStep.asString(keywords);
		}
    
    }
    
    public class ParametrisedStep extends AbstractStep {
        private Object[] convertedParameters;
        private String parametrisedStep;
        private final String stepAsString;
        private final Method method;
        private final String stepWithoutStartingWord;
        private final Map<String, String> namedParameters;

        public ParametrisedStep(String stepAsString, Method method, String stepWithoutStartingWord,
                Map<String, String> namedParameters) {
            this.stepAsString = stepAsString;
            this.method = method;
            this.stepWithoutStartingWord = stepWithoutStartingWord;
            this.namedParameters = namedParameters;
        }

        public void describeTo(StoryReporter storyReporter) {
            storyReporter.beforeStep(stepAsString);
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            Timer timer = new Timer().start();
            try {
                parametriseStep();
                stepMonitor.performing(parametrisedStep, dryRun);
                if (!dryRun) {
                    method.invoke(stepsInstance(), convertedParameters);
                }
                return successful(stepAsString).withParameterValues(parametrisedStep)
                        .setTimings(timer.stop());
            } catch (ParameterNotFound e) {
                // step parametrisation failed, return pending StepResult
                return pending(stepAsString).withParameterValues(parametrisedStep);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RestartingScenarioFailure) {
                    throw (RestartingScenarioFailure) e.getCause();
                }
                if (e.getCause() instanceof IgnoringStepsFailure) {
                    throw (IgnoringStepsFailure) e.getCause();
                }
                Throwable failureCause = e.getCause();
                if (failureCause instanceof UUIDExceptionWrapper) {
                    failureCause = failureCause.getCause();
                }
                return failed(stepAsString, new UUIDExceptionWrapper(stepAsString, failureCause)).withParameterValues(
                        parametrisedStep).setTimings(timer.stop());
            } catch (Throwable t) {
                return failed(stepAsString, new UUIDExceptionWrapper(stepAsString, t)).withParameterValues(
                        parametrisedStep).setTimings(timer.stop());
            }
        }

        public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
            try {
                parametriseStep();
            } catch (Throwable t) {
                // step parametrisation failed, but still return
                // notPerformed StepResult
            }
            return notPerformed(stepAsString).withParameterValues(parametrisedStep);
        }

		public String asString(Keywords keywords) {
			if ( parametrisedStep == null){
				parametriseStep();
			}
        	return parametrisedStep;
        }
        
        private void parametriseStep() {
            stepMatcher.find(stepWithoutStartingWord);
            ParameterName[] names = parameterNames(method);
            Type[] types = method.getGenericParameterTypes();
            String[] parameterValues = parameterValuesForStep(namedParameters, types, names);
            convertedParameters = convertParameterValues(parameterValues, types);
            addNamedParametersToExamplesTables();
            parametrisedStep = parametrisedStep(stepAsString, namedParameters, types, names, parameterValues);
        }

        private void addNamedParametersToExamplesTables() {
            for (Object object : convertedParameters) {
                if (object instanceof ExamplesTable) {
                    ((ExamplesTable) object).withNamedParameters(namedParameters);
                }
            }
        }

    }

    public class UponAnyParametrisedStep extends AbstractStep {
        private ParametrisedStep parametrisedStep;

        public UponAnyParametrisedStep(String stepAsString, Method method, String stepWithoutStartingWord,
                Map<String, String> namedParameters){
            this.parametrisedStep = new ParametrisedStep(stepAsString, method, stepWithoutStartingWord, namedParameters);
        }

        public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return perform(storyFailureIfItHappened);
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return parametrisedStep.perform(storyFailureIfItHappened);
        }

		public String asString(Keywords keywords) {
			return parametrisedStep.asString(keywords);
		}

    }

    public class UponSuccessParametrisedStep extends AbstractStep {
        private ParametrisedStep parametrisedStep;

        public UponSuccessParametrisedStep(String stepAsString, Method method, String stepWithoutStartingWord,
                Map<String, String> namedParameters){
            this.parametrisedStep = new ParametrisedStep(stepAsString, method, stepWithoutStartingWord, namedParameters);
        }

        public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return skipped();
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return parametrisedStep.perform(storyFailureIfItHappened);
        }

		public String asString(Keywords keywords) {
			return parametrisedStep.asString(keywords);
		}

    }

    public class UponFailureParametrisedStep extends AbstractStep {
        private ParametrisedStep parametrisedStep;

        public UponFailureParametrisedStep(String stepAsString, Method method, String stepWithoutStartingWord,
                Map<String, String> namedParameters){
            this.parametrisedStep = new ParametrisedStep(stepAsString, method, stepWithoutStartingWord, namedParameters);
        }

        public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return parametrisedStep.perform(storyFailureIfItHappened);
        }

        public StepResult perform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return skipped();
        }

		public String asString(Keywords keywords) {
			return parametrisedStep.asString(keywords);
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

        public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
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

		public String asString(Keywords keywords) {
			return stepAsString;
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

        public StepResult doNotPerform(UUIDExceptionWrapper storyFailureIfItHappened) {
            return ignorable(stepAsString);
        }
        
		public String asString(Keywords keywords) {
			return stepAsString;
		}

    }

    private class MethodInvoker {
        private final Method method;
        private final ParameterConverters parameterConverters;
        private final Paranamer paranamer;
        private final Meta meta;
        private final Type[] parameterTypes;
        
        public MethodInvoker(Method method, ParameterConverters parameterConverters, Paranamer paranamer, Meta meta) {
            this.method = method;
            this.parameterConverters = parameterConverters;
            this.paranamer = paranamer;
            this.meta = meta;
            this.parameterTypes = method.getGenericParameterTypes();
        }

        public void invoke() throws InvocationTargetException, IllegalAccessException {
            method.invoke(stepsInstance(), parameterValuesFrom(meta));
        }

        private Parameter[] methodParameters() {
            Parameter[] parameters = new Parameter[parameterTypes.length];
            String[] annotatedNames = annotatedParameterNames(method);
            String[] paranamerNames = paranamer.lookupParameterNames(method, false);

            for (int position = 0; position < parameterTypes.length; position++) {
                String name = parameterNameFor(position, annotatedNames, paranamerNames);
                parameters[position] = new Parameter(position, parameterTypes[position], name);
            }

            return parameters;
        }

        private String parameterNameFor(int position, String[] annotatedNames, String[] paranamerNames) {
            String annotatedName = nameByPosition(annotatedNames, position);
            String paranamerName = nameByPosition(paranamerNames, position);
            if (annotatedName != null) {
                return annotatedName;
            } else if (paranamerName != null) {
                return paranamerName;
            }
            return null;
        }

        private String nameByPosition(String[] names, int position) {
            return position < names.length ? names[position] : null;
        }

        private Object[] parameterValuesFrom(Meta meta) {
            Object[] values = new Object[parameterTypes.length];
            for (Parameter parameter : methodParameters()) {
            	values[parameter.position] = parameterConverters.convert(parameter.valueFrom(meta), parameter.type);
            }
            return values;
        }

        private class Parameter {
            private final int position;
            private final Type type;
            private final String name;

            public Parameter(int position, Type type, String name) {
                this.position = position;
                this.type = type;
                this.name = name;
            }

            public String valueFrom(Meta meta) {
                if (name == null) {
                    return null;
                }
                return meta.getProperty(name);
            }
        }
    }

    private static class ParameterName {
        private String name;
        private boolean annotated;

        private ParameterName(String name, boolean annotated) {
            this.name = name;
            this.annotated = annotated;
        }
    }

}
