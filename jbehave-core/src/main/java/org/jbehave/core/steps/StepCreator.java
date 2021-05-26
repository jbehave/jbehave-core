package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.jbehave.core.steps.AbstractStepResult.comment;
import static org.jbehave.core.steps.AbstractStepResult.failed;
import static org.jbehave.core.steps.AbstractStepResult.ignorable;
import static org.jbehave.core.steps.AbstractStepResult.notPerformed;
import static org.jbehave.core.steps.AbstractStepResult.pending;
import static org.jbehave.core.steps.AbstractStepResult.silent;
import static org.jbehave.core.steps.AbstractStepResult.skipped;
import static org.jbehave.core.steps.AbstractStepResult.successful;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.FromContext;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.ToContext;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.IgnoringStepsFailure;
import org.jbehave.core.failures.RestartingScenarioFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Verbatim;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.context.StepsContext;

public class StepCreator {

    public static final String PARAMETER_TABLE_START = "\uff3b";
    public static final String PARAMETER_TABLE_END = "\uff3d";
    public static final String PARAMETER_VERBATIM_START = "\u301a";
    public static final String PARAMETER_VERBATIM_END = "\u301b";
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
    private final StepsContext stepsContext;
    private StepMonitor stepMonitor;
    private Paranamer paranamer = new NullParanamer();
    private boolean dryRun = false;

    public StepCreator(Class<?> stepsType, InjectableStepsFactory stepsFactory,
            StepsContext stepsContext, ParameterConverters parameterConverters, ParameterControls parameterControls,
            StepMatcher stepMatcher, StepMonitor stepMonitor) {
        this.stepsType = stepsType;
        this.stepsFactory = stepsFactory;
        this.stepsContext = stepsContext;
        this.parameterConverters = parameterConverters;
        this.parameterControls = parameterControls;
        this.stepMatcher = stepMatcher;
        this.stepMonitor = stepMonitor;
        this.delimitedNamePattern = Pattern.compile(parameterControls.nameDelimiterLeft() + "([\\w\\-]+?)"
                + parameterControls.nameDelimiterRight(), Pattern.DOTALL);
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
        Step beforeOrAfterStep = createBeforeOrAfterStep(method, storyAndScenarioMeta);
        return wrapStepUponOutcome(outcome, beforeOrAfterStep);
    }

    public Map<String, String> matchedParameters(final Method method, final String stepWithoutStartingWord,
            final Map<String, String> namedParameters) {
        Map<String, String> matchedParameters = new HashMap<>();
        Matcher matcher = stepMatcher.matcher(stepWithoutStartingWord);
        if (matcher.find()) {
            // we've found a match, populate map
            ParameterName[] parameterNames = parameterNames(method);
            Type[] types = parameterTypes(method, parameterNames);

            String[] values = parameterValuesForStep(matcher, namedParameters, types, parameterNames, false);
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
        ParameterName[] parameterNames;
        if (method != null) {
            String[] annotatedNames = annotatedParameterNames(method);
            String[] paranamerNames = paranamerParameterNames(method);
            String[] contextNames = contextParameterNames(method);

            parameterNames = new ParameterName[annotatedNames.length];
            for (int i = 0; i < annotatedNames.length; i++) {
                parameterNames[i] = parameterName(annotatedNames, paranamerNames, contextNames, i);
            }
        } else {
            String[] stepMatcherParameterNames = stepMatcher.parameterNames();
            parameterNames = new ParameterName[stepMatcherParameterNames.length];
            for (int i = 0; i < stepMatcherParameterNames.length; i++) {
                parameterNames[i] = new ParameterName(stepMatcherParameterNames[i], false, false);
            }
        }
        return parameterNames;
    }

    private ParameterName parameterName(String[] annotatedNames, String[] paranamerNames, String[] contextNames, int i) {
        boolean annotated = true;
        boolean fromContext = false;

        String name = contextNames[i];
        if (name != null) {
            fromContext = true;
        } else {
            name = annotatedNames[i];
            if (name == null) {
                name = (paranamerNames.length > i ? paranamerNames[i] : null);
                annotated = false;
            }
        }
        return new ParameterName(name, annotated, fromContext);
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
     * Extract parameter names using {@link FromContext}-annotated parameters
     * 
     * @param method the Method with {@link FromContext}-annotated parameters
     * @return An array of annotated parameter names, which <b>may</b> include
     *         <code>null</code> values for parameters that are not annotated
     */
    private String[] contextParameterNames(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        String[] names = new String[parameterAnnotations.length];
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                names[i] = contextName(annotation);
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
     * Returns the value of the annotation {@link FromContext}.
     * 
     * @param annotation the Annotation
     * @return The annotated value or <code>null</code> if no annotation is
     *         found
     */
    private String contextName(Annotation annotation) {
        if (annotation.annotationType().isAssignableFrom(FromContext.class)) {
            return ((FromContext) annotation).value();
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

    private Type[] parameterTypes(Method method, ParameterName[] parameterNames) {
        if (method != null) {
            return method.getGenericParameterTypes();
        }
        Type[] types = new Type[parameterNames.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = String.class;
        }
        return types;
    }

    public Step createParametrisedStep(final Method method, final String stepAsString,
            final String stepWithoutStartingWord, final Map<String, String> namedParameters,
            final List<Step> composedSteps) {
        return new ParametrisedStep(stepAsString, method, stepWithoutStartingWord, namedParameters, composedSteps);
    }

    public Step createParametrisedStepUponOutcome(final Method method, final String stepAsString,
            final String stepWithoutStartingWord, final Map<String, String> namedParameters,
            final List<Step> composedSteps, Outcome outcome) {
        Step parametrisedStep = createParametrisedStep(method, stepAsString, stepWithoutStartingWord, namedParameters,
                composedSteps);
        return wrapStepUponOutcome(outcome, parametrisedStep);
    }

    private Step wrapStepUponOutcome(Outcome outcome, Step step) {
        switch (outcome) {
            case ANY:
                return new UponAnyStep(step);
            case SUCCESS:
                return new UponSuccessStep(step);
            case FAILURE:
                return new UponFailureStep(step);
            default:
                return step;
        }
    }

    private String parametrisedStep(String stepAsString, Map<String, String> namedParameters, Type[] types,
            String[] parameterValues) {
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
            if (isTable(type)) {
                return true;
            }
        }
        return false;
    }

    private String markNamedParameterValue(String stepText, Map<String, String> namedParameters, String name) {
        String value = namedParameter(namedParameters, name);
        if (value != null) {
            return parameterControls.replaceAllDelimitedNames(stepText, name, markedValue(value));
        }
        return stepText;
    }

    private String markParsedParameterValue(String stepText, Type type, String value, boolean hasTable) {
        if (value != null) {
            // only mark non-empty string as parameter (JBEHAVE-656)
            if (value.trim().length() != 0) {
                if (isTable(type)) {
                    return stepText.replace(value, markedTable(value));
                }
                if (isVerbatim(type)) {
                    return stepText.replace(value, markedVerbatim(value));
                }
                String markedValue = markedValue(value);
                // identify parameter values to mark as padded by spaces to avoid duplicated replacements of overlapping values (JBEHAVE-837)
                String leftPad = SPACE;
                String rightPad = stepText.endsWith(value) ? NONE : SPACE;
                return stepText.replace(pad(value, leftPad, rightPad), pad(markedValue, leftPad, rightPad));
            }
            if (!hasTable) {
                return stepText.replace(NEWLINE, PARAMETER_VALUE_NEWLINE);
            }
        }
        return stepText;
    }

    private String markedTable(String value) {
        return pad(value, PARAMETER_TABLE_START, PARAMETER_TABLE_END);
    }

    private String markedVerbatim(String value) {
        return pad(value, PARAMETER_VERBATIM_START, PARAMETER_VERBATIM_END);
    }

    private String markedValue(String value) {
        return pad(value, PARAMETER_VALUE_START, PARAMETER_VALUE_END);
    }

    private String pad(String value, String left, String right) {
        return new StringBuilder().append(left).append(value).append(right).toString();
    }

    private boolean isTable(Type type) {
        return isExamplesTable(type) || isExamplesTableParameters(type);
    }

    private boolean isVerbatim(Type type) {
        return type instanceof Class && Verbatim.class.isAssignableFrom((Class<?>) type);
    }

    private boolean isExamplesTable(Type type) {
        return type instanceof Class && ExamplesTable.class.isAssignableFrom((Class<?>) type);
    }

    private boolean isExamplesTableParameters(Type type) {
        boolean result = false;

        if (type instanceof Class) {
            ((Class) type).isAnnotationPresent(AsParameters.class);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            result = isExamplesTableParameters(rawClass(parameterizedType)) || isExamplesTableParameters(argumentClass(parameterizedType));
        }

        return result;
    }

    private boolean isExamplesTableParameters(Class type) {
        return type != null && type.isAnnotationPresent(AsParameters.class);
    }

    private Class<?> rawClass(ParameterizedType type) {
        Class result = null;

        Type rawType = type.getRawType();
        if (rawType instanceof Class) {
            result = (Class) rawType;
        }

        return result;
    }

    private Class<?> argumentClass(ParameterizedType type) {
        Class result = null;

        Type[] typeArguments = type.getActualTypeArguments();
        if (typeArguments.length > 0) {
            Type argument = typeArguments[0];
            if (argument instanceof Class) {
                result = (Class) argument;
            }
        }

        return result;
    }

    private String[] parameterValuesForStep(Matcher matcher, Map<String, String> namedParameters, Type[] types,
            ParameterName[] names, boolean overrideWithTableParameters) {
        final String[] parameters = new String[types.length];
        for (int position = 0; position < types.length; position++) {
            parameters[position] = parameterForPosition(matcher, position, names, namedParameters,
                    overrideWithTableParameters);
        }
        return parameters;
    }

    private Object[] convertParameterValues(String[] valuesAsString, Type[] types, ParameterName[] names) {
        final Object[] parameters = new Object[valuesAsString.length];
        for (int position = 0; position < valuesAsString.length; position++) {
            if (names[position].fromContext) {
                parameters[position] = stepsContext.get(valuesAsString[position]);
            } else {
                parameters[position] = parameterConverters.convert(valuesAsString[position], types[position]);
            }
        }
        return parameters;
    }

    private String parameterForPosition(Matcher matcher, int position, ParameterName[] names,
            Map<String, String> namedParameters, boolean overrideWithTableParameters) {
        int namePosition = parameterPosition(names, position);
        String parameter = null;

        if (namePosition != -1) {
            String name = names[position].name;
            boolean annotated = names[position].annotated;
            boolean fromContext = names[position].fromContext;

            List<String> delimitedNames = Collections.emptyList();

            if (isGroupName(name)) {
                parameter = matchedParameter(matcher, name);
                delimitedNames = delimitedNameFor(parameter);

                if (delimitedNames.isEmpty()) {
                    monitorUsingNameForParameter(name, position, annotated);
                }
            }

            if (!delimitedNames.isEmpty()) {
                parameter = replaceAllDelimitedNames(delimitedNames, position, annotated, parameter, namedParameters);
                delimitedNames = delimitedNameFor(parameter);
                if (!delimitedNames.isEmpty()) {
                    parameter = replaceAllDelimitedNames(delimitedNames, position, annotated, parameter,
                            namedParameters);
                }
            } else if (overrideWithTableParameters && isTableName(namedParameters, name)) {
                parameter = namedParameter(namedParameters, name);
                if (parameter != null) {
                    monitorUsingTableNameForParameter(name, position, annotated); 
                }
            }
            
            if (fromContext && parameter == null) {
                parameter = name;
                stepMonitor.usingStepsContextParameter(parameter);
            }

        }

        if (parameter == null) {
            // This allow parameters to be in different order.
            position = position - numberOfPreviousFromContext(names, position);
            stepMonitor.usingNaturalOrderForParameter(position);
            parameter = matchedParameter(matcher, position);
            List<String> delimitedNames = delimitedNameFor(parameter);

            for (String delimitedName : delimitedNames) {
                if (isTableName(namedParameters, delimitedName)) {
                    parameter = parameterControls.replaceAllDelimitedNames(parameter, delimitedName,
                            namedParameter(namedParameters, delimitedName));
                }
            }
        }

        stepMonitor.foundParameter(parameter, position);

        return parameter;
    }

    private String replaceAllDelimitedNames(List<String> delimitedNames, int position, boolean annotated,
                                            String parameter, Map<String, String> namedParameters) {
        String parameterWithDelimitedNames = parameter;
        for (String delimitedName : delimitedNames) {
            monitorUsingTableNameForParameter(delimitedName, position, annotated);
            parameterWithDelimitedNames = parameterControls.replaceAllDelimitedNames(parameterWithDelimitedNames,
                    delimitedName, namedParameter(namedParameters, delimitedName));
        }
        return parameterWithDelimitedNames;
    }

    private int numberOfPreviousFromContext(ParameterName[] names, int currentPosition) {
        int number = 0;

        for (int i = currentPosition - 1; i >= 0; i--) {
            if (names[i].fromContext) {
                number++;
            }
        }
        
        return number;
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

    private List<String> delimitedNameFor(String parameter) {
        List<String> delimitedNames = new ArrayList<>();
        if (parameterControls.delimiterNamedParameters()) {
            Matcher matcher = delimitedNamePattern.matcher(parameter);
            while (matcher.find()) {
                delimitedNames.add(matcher.group(1));
            }
        }
        return delimitedNames;
    }

    String matchedParameter(Matcher matcher, String name) {
        String[] parameterNames = stepMatcher.parameterNames();
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            if (name.equals(parameterName)) {
                return matchedParameter(matcher, i);
            }
        }
        throw new ParameterNotFound(name, parameterNames);
    }

    private String matchedParameter(Matcher matcher, int position) {
        String[] parameterNames = stepMatcher.parameterNames();
        int matchedPosition = position + 1;
        if (matchedPosition <= parameterNames.length) {
            return matcher.group(matchedPosition);
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
            if (name != null && name.equals(positionName)) {
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

    public static Step createComment(final String stepAsString) {
        return new Comment(stepAsString);
    }

    private void storeOutput(Object object, Method method) {
        ToContext annotation = method.getAnnotation(ToContext.class);
        if (annotation != null) {
            stepsContext.put(annotation.value(), object, annotation.retentionLevel());
        }
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

    public abstract static class AbstractStep implements Step {

        @Override
        public String asString(Keywords keywords) {
            return toString();
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }

        @Override
        public List<Step> getComposedSteps() {
            return Collections.emptyList();
        }
    }

    public abstract static class ReportingAbstractStep extends AbstractStep {

        private final StepExecutionType stepExecutionType;
        private final String stepAsString;

        public ReportingAbstractStep(StepExecutionType stepExecutionType, String stepAsString) {
            this.stepExecutionType = stepExecutionType;
            this.stepAsString = stepAsString;
        }

        @Override
        public final StepResult perform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            storyReporter.beforeStep(new org.jbehave.core.model.Step(stepExecutionType, getStepAsString()));
            return perform();
        }

        protected abstract StepResult perform();

        @Override
        public String asString(Keywords keywords) {
            return stepAsString;
        }

        protected String getStepAsString() {
            return stepAsString;
        }
    }

    static class DelegatingStep extends AbstractStep {
        private final Step step;

        DelegatingStep(Step step) {
            this.step = step;
        }

        @Override
        public StepResult perform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return step.perform(storyReporter, storyFailureIfItHappened);
        }

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return step.doNotPerform(storyReporter, storyFailureIfItHappened);
        }

        @Override
        public String asString(Keywords keywords) {
            return step.asString(keywords);
        }

        @Override
        public List<Step> getComposedSteps() {
            return step.getComposedSteps();
        }
    }

    private class BeforeOrAfterStep extends AbstractStep {
        private final Method method;
        private final Meta meta;

        public BeforeOrAfterStep(Method method, Meta meta) {
            this.method = method;
            this.meta = meta;
        }

        @Override
        public StepResult perform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            ParameterConverters paramConvertersWithExceptionInjector = paramConvertersWithExceptionInjector(storyFailureIfItHappened);
            MethodInvoker methodInvoker = new MethodInvoker(method, paramConvertersWithExceptionInjector, paranamer,
                    meta);
            Timer timer = new Timer().start();
            try {
                Object outputObject = methodInvoker.invoke();
                storeOutput(outputObject, method);
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

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return perform(storyReporter, storyFailureIfItHappened);
        }

        private class UUIDExceptionWrapperInjector implements ParameterConverter<UUIDExceptionWrapper> {
            private final UUIDExceptionWrapper storyFailureIfItHappened;

            public UUIDExceptionWrapperInjector(UUIDExceptionWrapper storyFailureIfItHappened) {
                this.storyFailureIfItHappened = storyFailureIfItHappened;
            }

            @Override
            public boolean canConvertTo(Type type) {
                return UUIDExceptionWrapper.class == type;
            }

            @Override
            public UUIDExceptionWrapper convertValue(String value, Type type) {
                return storyFailureIfItHappened;
            }
        }

        @Override
        public String asString(Keywords keywords) {
            return method.getName() + ";" + meta.asString(keywords);
        }
    }

    class UponAnyStep extends DelegatingStep {

        UponAnyStep(Step step) {
            super(step);
        }

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return perform(storyReporter, storyFailureIfItHappened);
        }
    }

    class UponSuccessStep extends DelegatingStep {

        UponSuccessStep(Step step) {
            super(step);
        }

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return skipped();
        }
    }

    class UponFailureStep extends DelegatingStep {

        UponFailureStep(Step step) {
            super(step);
        }

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return super.perform(storyReporter, storyFailureIfItHappened);
        }

        @Override
        public StepResult perform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return skipped();
        }
    }

    public class ParametrisedStep extends ReportingAbstractStep {
        private Object[] convertedParameters;
        private String parametrisedStep;
        private final Method method;
        private final String stepWithoutStartingWord;
        private final Map<String, String> namedParameters;
        private final List<Step> composedSteps;

        public ParametrisedStep(String stepAsString, Method method, String stepWithoutStartingWord,
                Map<String, String> namedParameters, List<Step> composedSteps) {
            super(StepExecutionType.EXECUTABLE, stepAsString);
            this.method = method;
            this.stepWithoutStartingWord = stepWithoutStartingWord;
            this.namedParameters = namedParameters;
            this.composedSteps = composedSteps;
        }

        @Override
        public List<Step> getComposedSteps() {
            return composedSteps;
        }

        @Override
        public StepResult perform() {
            String stepAsString = getStepAsString();
            Timer timer = new Timer().start();
            try {
                parametriseStep();
                stepMonitor.beforePerforming(parametrisedStep, dryRun, method);
                if (!dryRun && method != null) {
                    Object outputObject = method.invoke(stepsInstance(), convertedParameters);
                    storeOutput(outputObject, method);
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
            } finally {
                stepMonitor.afterPerforming(parametrisedStep, dryRun, method);
            }
        }

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            storyReporter.beforeStep(
                    new org.jbehave.core.model.Step(StepExecutionType.NOT_PERFORMED, getStepAsString()));
            try {
                parametriseStep();
            } catch (Throwable t) {
                // step parametrisation failed, but still return
                // notPerformed StepResult
            }
            return notPerformed(getStepAsString()).withParameterValues(parametrisedStep);
        }

        @Override
        public String asString(Keywords keywords) {
            if (parametrisedStep == null) {
                parametriseStep();
            }
            return parametrisedStep;
        }
        
        private void parametriseStep() {
            Matcher matcher = stepMatcher.matcher(stepWithoutStartingWord);
            matcher.find();
            ParameterName[] names = parameterNames(method);
            Type[] types = parameterTypes(method, names);
            String[] parameterValues = parameterValuesForStep(matcher, namedParameters, types, names, true);
            convertedParameters = method == null ? parameterValues
                    : convertParameterValues(parameterValues, types, names);
            addNamedParametersToExamplesTables();
            parametrisedStep = parametrisedStep(getStepAsString(), namedParameters, types, parameterValues);
        }

        private void addNamedParametersToExamplesTables() {
            for (Object object : convertedParameters) {
                if (object instanceof ExamplesTable) {
                    ((ExamplesTable) object).withNamedParameters(namedParameters);
                }
            }
        }
    }

    public static class PendingStep extends ReportingAbstractStep {
        private final String previousNonAndStep;
        private Method method;

        public PendingStep(String stepAsString, String previousNonAndStep) {
            super(StepExecutionType.PENDING, stepAsString);
            this.previousNonAndStep = previousNonAndStep;
        }

        @Override
        protected StepResult perform() {
            return pending(getStepAsString());
        }

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return perform(storyReporter, storyFailureIfItHappened);
        }

        public String stepAsString() {
            return getStepAsString();
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

    public static class IgnorableStep extends ReportingAbstractStep {

        public IgnorableStep(String stepAsString) {
            super(StepExecutionType.IGNORABLE, stepAsString);
        }

        @Override
        protected StepResult perform() {
            return ignorable(getStepAsString());
        }

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return perform(storyReporter, storyFailureIfItHappened);
        }
    }

    public static class Comment extends ReportingAbstractStep {

        public Comment(String stepAsString) {
            super(StepExecutionType.COMMENT, stepAsString);
        }

        @Override
        protected StepResult perform() {
            return comment(getStepAsString());
        }

        @Override
        public StepResult doNotPerform(StoryReporter storyReporter, UUIDExceptionWrapper storyFailureIfItHappened) {
            return perform(storyReporter, storyFailureIfItHappened);
        }
    }

    public static enum StepExecutionType {
        EXECUTABLE,
        PENDING,
        IGNORABLE,
        COMMENT,
        NOT_PERFORMED;
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

        public Object invoke() throws InvocationTargetException, IllegalAccessException {
            return method.invoke(stepsInstance(), parameterValuesFrom(meta));
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
        private boolean fromContext;

        private ParameterName(String name, boolean annotated, boolean fromContext) {
            this.name = name;
            this.annotated = annotated;
            this.fromContext = fromContext;
        }
    }
}
