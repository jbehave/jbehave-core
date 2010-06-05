package org.jbehave.core.steps;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import org.jbehave.core.annotations.Named;
import org.jbehave.core.errors.PendingError;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.parsers.StepPatternParser;

import com.thoughtworks.paranamer.NullParanamer;
import com.thoughtworks.paranamer.Paranamer;


/**
 * Creates candidate step from a regex pattern of a step of a given type,
 * associated to a Java method.
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 * @author Paul Hammant
 */
public class CandidateStep {

    public static final String PARAMETER_NAME_START = "<";
    public static final String PARAMETER_NAME_END = ">";
    public static final String PARAMETER_VALUE_START = "\uFF5F";
    public static final String PARAMETER_VALUE_END = "\uFF60";
    private final String patternAsString;
    private final Integer priority;
    private final StepType stepType;
    private final Method method;
    private final Object stepsInstance;
    private final ParameterConverters parameterConverters;
    private final Map<StepType, String> startingWordsByType;
	private final StepMatcher stepMatcher;
    private StepMonitor stepMonitor = new SilentStepMonitor();
    private Paranamer paranamer = new NullParanamer();
    private boolean dryRun = false;
    
    public CandidateStep(String patternAsString, int priority, StepType stepType, Method method,
            CandidateSteps steps, StepPatternParser patternParser,
            ParameterConverters parameterConverters, Map<StepType, String> startingWords) {
        this(patternAsString, priority, stepType, method, (Object) steps, patternParser, parameterConverters, startingWords);
    }

    public CandidateStep(String patternAsString, int priority, StepType stepType, Method method,
            Object stepsInstance, StepPatternParser patternParser,
            ParameterConverters parameterConverters, Map<StepType, String> startingWords) {
        this.patternAsString = patternAsString;
        this.priority = priority;
        this.stepType = stepType;
        this.method = method;
        this.stepsInstance = stepsInstance;
        this.parameterConverters = parameterConverters;
        this.startingWordsByType = startingWords;
        this.stepMatcher = patternParser.parseStep(patternAsString);
    }

 	public Integer getPriority() {
        return priority;
    }
	
    public StepType getStepType() {
        return stepType;
    }

    public String getPatternAsString() {
        return patternAsString;
    }

    public boolean dryRun() {
		return dryRun;
	}

	public void doDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

    public void useStepMonitor(StepMonitor stepMonitor) {
        this.stepMonitor = stepMonitor;
    }

    public void useParanamer(Paranamer paranamer) {
        this.paranamer = paranamer;
    }

    public boolean ignore(String stepAsString) {
        try {
            String ignoreWord = startingWordFor(StepType.IGNORABLE);
            return stepAsString.startsWith(ignoreWord);
        } catch (StartingWordNotFound e) {
            return false;
        }
    }

    public boolean matches(String stepAsString) {
    	return matches(stepAsString, null);
    }

    public boolean matches(String step, String previousNonAndStep) {
        try {
        	boolean matchesType = true;
        	if ( isAndStep(step) ){
        		if ( previousNonAndStep == null ){
        			matchesType = false; // cannot handle AND step with no previous step
        		} else {
        			// previous step type should match candidate step type
        			matchesType = startingWordFor(stepType).equals(findStartingWord(previousNonAndStep));
        		}
        	}
            stepMonitor.stepMatchesType(step, previousNonAndStep, matchesType, stepType, method, stepsInstance);
            boolean matchesPattern = stepMatcher.matches(stripStartingWord(step));
            stepMonitor.stepMatchesPattern(step, matchesPattern, stepMatcher.pattern(), method, stepsInstance);
            // must match both type and pattern
            return matchesType && matchesPattern;
        } catch (StartingWordNotFound e) {
            return false;
        }
    }

	public boolean isAndStep(String stepAsString) {
		return stepAsString.startsWith(startingWordFor(StepType.AND));
	}

    public Step createFrom(Map<String, String> tableRow, final String stepAsString) {
        stepMatcher.find(stripStartingWord(stepAsString));
        return createStep(stepAsString, tableRow, method, stepMonitor);
    }

    private String stripStartingWord(final String stepAsString) {
		String startingWord = findStartingWord(stepAsString);
        return trimStartingWord(startingWord, stepAsString);
	}

    private String findStartingWord(final String stepAsString) throws StartingWordNotFound {
        String wordForType = startingWordFor(stepType);
        if (stepAsString.startsWith(wordForType)) {
            return wordForType;
        }
        String andWord = startingWordFor(StepType.AND);
        if (stepAsString.startsWith(andWord)) {
            return andWord;
        }
        throw new StartingWordNotFound(stepAsString, stepType, startingWordsByType);
    }

    private String trimStartingWord(String word, String step) {
        return step.substring(word.length() + 1); // 1 for the space after
    }

    private String startingWordFor(StepType stepType) {
        String startingWord = startingWordsByType.get(stepType);
        if (startingWord == null) {
            throw new StartingWordNotFound(stepType, startingWordsByType);
        }
        return startingWord;
    }

    private Step createStep(final String stepAsString, Map<String, String> tableRow, final Method method, final StepMonitor stepMonitor) {
        Type[] types = method.getGenericParameterTypes();
        String[] annotationNames = annotatedParameterNames();
        String[] parameterNames = paranamer.lookupParameterNames(method, false);
        final String parametrisedStep = parametrisedStep(stepAsString, tableRow, types, annotationNames, parameterNames);
        final Object[] args = argsForStep(tableRow, types, annotationNames, parameterNames);
        return new Step() {
            public StepResult perform() {
                try {
					stepMonitor.performing(stepAsString, dryRun);
					if (!dryRun) {
						method.invoke(stepsInstance, args);
					}
                    return StepResult.successful(stepAsString).withParameterValues(parametrisedStep);
                } catch (Throwable t) {
                    return failedOrPending(stepAsString, t);
                }
            }

            private StepResult failedOrPending(final String stepAsString, Throwable t) {
                if (t instanceof InvocationTargetException && t.getCause() != null) {
                    Throwable cause = t.getCause();
					if (cause instanceof PendingError) {
                        return StepResult.pending(stepAsString, (PendingError) cause).withParameterValues(parametrisedStep);
                    } else {
                        return StepResult.failed(stepAsString, cause).withParameterValues(parametrisedStep);
                    }
                }
                return StepResult.failed(stepAsString, t).withParameterValues(parametrisedStep);
            }

            public StepResult doNotPerform() {
                return StepResult.notPerformed(stepAsString).withParameterValues(parametrisedStep);
            }

        };
    }
    
    /**
     * Extract annotated parameter names from the @Named parameter annotations
     * 
     * @return An array of annotated parameter names, which <b>may</b> include
     *         <code>null</code> values for parameters that are not annotated
     */
    private String[] annotatedParameterNames() {
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
    
    private String parametrisedStep(String stepAsString, Map<String, String> tableRow, Type[] types, String[] annotationNames,
            String[] parameterNames) {
        String parametrisedStep = stepAsString;
        for (int position = 0; position < types.length; position++) {
            parametrisedStep = replaceParameterValuesInStep(parametrisedStep, position, annotationNames, parameterNames, tableRow);
        }
        return parametrisedStep;
    }

    private String replaceParameterValuesInStep(String stepText, int position, String[] annotationNames, String[] parameterNames,
            Map<String, String> tableRow) {
        int annotatedNamePosition = parameterPosition(annotationNames, position);
        int parameterNamePosition = parameterPosition(parameterNames, position);
        if (annotatedNamePosition != -1) {
            String name = annotationNames[position];
            String value = getTableValue(tableRow, name);
            if (value != null) {
                stepText = stepText.replace(PARAMETER_NAME_START + name + PARAMETER_NAME_END, PARAMETER_VALUE_START + value + PARAMETER_VALUE_END);
            }
        } else if (parameterNamePosition != -1) {
            String name = parameterNames[position];
            String value = getTableValue(tableRow, name);
            if (value != null) {
                stepText = stepText.replace(PARAMETER_NAME_START + name + PARAMETER_NAME_END, PARAMETER_VALUE_START + value + PARAMETER_VALUE_START);
            }
        }
        return stepText;
    }
    
    private Object[] argsForStep(Map<String, String> tableRow, Type[] types, String[] annotationNames,
            String[] parameterNames) {
        final Object[] args = new Object[types.length];
        for (int position = 0; position < types.length; position++) {
            String arg = argForPosition(position, annotationNames, parameterNames, tableRow);
            args[position] = parameterConverters.convert(arg, types[position]);
        }
        return args;
    }

    private String argForPosition(int position, String[] annotationNames, String[] parameterNames,
            Map<String, String> tableRow) {
        int annotatedNamePosition = parameterPosition(annotationNames, position);
        int parameterNamePosition = parameterPosition(parameterNames, position);
        String arg = null;
        if (annotatedNamePosition != -1 && isGroupName(annotationNames[position])) {
            String name = annotationNames[position];
            stepMonitor.usingAnnotatedNameForArg(name, position);
            arg = matchedParameter(name);
        } else if (parameterNamePosition != -1 && isGroupName(parameterNames[position])) {
            String name = parameterNames[position];
            stepMonitor.usingParameterNameForArg(name, position);
            arg = matchedParameter(name);
        } else if (annotatedNamePosition != -1 && isTableFieldName(tableRow, annotationNames[position])) {
            String name = annotationNames[position];
            stepMonitor.usingTableAnnotatedNameForArg(name, position);
            arg = getTableValue(tableRow, name);
        } else if (parameterNamePosition != -1 && isTableFieldName(tableRow, parameterNames[position])) {
            String name = parameterNames[position];
            stepMonitor.usingTableParameterNameForArg(name, position);
            arg = getTableValue(tableRow, name);
        } else {
            stepMonitor.usingNaturalOrderForArg(position);
            arg = matchedParameter(position);
        }
        stepMonitor.foundArg(arg, position);
        return arg;
    }
    
    private String matchedParameter(String name) {
        String[] parameterNames = stepMatcher.parameterNames();
		for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            if (name.equals(parameterName)) {
                return matchedParameter(i);
            }
        }
        throw new NoParameterFoundForName(name, parameterNames);
    }

	private String matchedParameter(int i) {
		return stepMatcher.parameter(i + 1);
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

    private String getTableValue(Map<String, String> tableRow, String name) {
        return tableRow.get(name);
    }

    private boolean isTableFieldName(Map<String, String> tableRow, String name) {
        return tableRow.get(name) != null;
    }

    @Override
    public String toString() {
        return stepType + " " + patternAsString;
    }

    @SuppressWarnings("serial")
    public static class StepNotMatchingPattern extends RuntimeException {

		public StepNotMatchingPattern(String stepAsString, String pattern) {
			super("Step "+stepAsString+" not matching pattern "+pattern);
		}

    }

    @SuppressWarnings("serial")
    public static class NoParameterFoundForName extends RuntimeException {

        public NoParameterFoundForName(String name, String[] names) {
            super("No parameter found for name '" + name + "' amongst '" + asList(names) + "'");
        }

    }

    @SuppressWarnings("serial")
    public static class StartingWordNotFound extends RuntimeException {

        public StartingWordNotFound(String step, StepType stepType, Map<StepType, String> startingWordsByType) {
            super("No starting word found for step '" + step + "' of type '" + stepType + "' amongst '"
                    + startingWordsByType+"'");
        }

        public StartingWordNotFound(StepType stepType, Map<StepType, String> startingWordsByType) {
            super("No starting word found of type '" + stepType + "' amongst '" + startingWordsByType+"'");
        }

    }
    
    /**
     * This is a different class, because the @Inject jar may not be in the classpath.
     */
    private static class Jsr330Helper {

        private static String getNamedValue(Annotation annotation) {
            return ((javax.inject.Named) annotation).value();
        }

    }


}