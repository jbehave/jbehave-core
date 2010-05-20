package org.jbehave.core.steps;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.annotations.Named;
import org.jbehave.core.errors.PendingError;
import org.jbehave.core.parser.StepPatternBuilder;

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
    private final Pattern pattern;
    private final String[] groupNames;

    private StepMonitor stepMonitor = new SilentStepMonitor();
    private Paranamer paranamer = new NullParanamer();
    private boolean dryRun = false;
    
    public CandidateStep(String patternAsString, int priority, StepType stepType, Method method,
            CandidateSteps steps, StepPatternBuilder patternBuilder,
            ParameterConverters parameterConverters, Map<StepType, String> startingWords) {
        this(patternAsString, priority, stepType, method, (Object) steps, patternBuilder, parameterConverters, startingWords);
    }

    public CandidateStep(String patternAsString, int priority, StepType stepType, Method method,
            Object stepsInstance, StepPatternBuilder patternBuilder,
            ParameterConverters parameterConverters, Map<StepType, String> startingWords) {
        this.patternAsString = patternAsString;
        this.priority = priority;
        this.stepType = stepType;
        this.method = method;
        this.stepsInstance = stepsInstance;
        this.parameterConverters = parameterConverters;
        this.startingWordsByType = startingWords;
        this.pattern = patternBuilder.buildPattern(patternAsString);
        this.groupNames = patternBuilder.extractGroupNames(patternAsString);
    }

    public void useStepMonitor(StepMonitor stepMonitor) {
        this.stepMonitor = stepMonitor;
    }

    public void useParanamer(Paranamer paranamer) {
        this.paranamer = paranamer;
    }

    protected Paranamer getParanamer() {
        return paranamer;
    }

    public boolean dryRun() {
		return dryRun;
	}

	public void doDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	public Integer getPriority() {
        return priority;
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
            boolean matchesPattern = matcherForStep(step).matches();
            stepMonitor.stepMatchesPattern(step, matchesPattern, pattern.pattern(), method, stepsInstance);
            // must match both type and pattern
            return matchesType && matchesPattern;
        } catch (StartingWordNotFound e) {
            return false;
        }
    }

	public boolean isAndStep(String stepAsString) {
		return stepAsString.startsWith(startingWordFor(StepType.AND));
	}

    private String trimStartingWord(String word, String step) {
        return step.substring(word.length() + 1); // 1 for the space after
    }

    public Step createFrom(Map<String, String> tableRow, final String stepAsString) {
        Matcher matcher = matcherForStep(stepAsString);
        matcher.find();
        return createStep(stepAsString, tableRow, matcher, method, stepMonitor, groupNames);
    }

    private Matcher matcherForStep(final String stepAsString) {
        String startingWord = findStartingWord(stepAsString);
        String trimmed = trimStartingWord(startingWord, stepAsString);
        return pattern.matcher(trimmed);
    }

    protected Object[] argsForStep(Map<String, String> tableRow, Matcher matcher, Type[] types, String[] annotationNames,
            String[] parameterNames) {
        final Object[] args = new Object[types.length];
        for (int position = 0; position < types.length; position++) {
            String arg = argForPosition(position, annotationNames, parameterNames, tableRow, matcher);
            args[position] = parameterConverters.convert(arg, types[position]);
        }
        return args;
    }

    protected String translatedStep(String stepAsString, Map<String, String> tableRow, Type[] types, String[] annotationNames,
            String[] parameterNames) {
        String replacedStepText = stepAsString;
        for (int position = 0; position < types.length; position++) {
            replacedStepText = replaceValuesInStepText(replacedStepText, position, annotationNames, parameterNames, tableRow);
        }
        return replacedStepText;
    }

    private String argForPosition(int position, String[] annotationNames, String[] parameterNames,
            Map<String, String> tableRow, Matcher matcher) {
        int annotatedNamePosition = parameterPosition(annotationNames, position);
        int parameterNamePosition = parameterPosition(parameterNames, position);
        String arg = null;
        if (annotatedNamePosition != -1 && isGroupName(annotationNames[position])) {
            String name = annotationNames[position];
            stepMonitor.usingAnnotatedNameForArg(name, position);
            arg = getGroup(matcher, name);
        } else if (parameterNamePosition != -1 && isGroupName(parameterNames[position])) {
            String name = parameterNames[position];
            stepMonitor.usingParameterNameForArg(name, position);
            arg = getGroup(matcher, name);
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
            arg = getGroup(matcher, position);
        }
        stepMonitor.foundArg(arg, position);
        return arg;
    }

    private String replaceValuesInStepText(String stepText, int position, String[] annotationNames, String[] parameterNames,
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


    private String getTableValue(Map<String, String> tableRow, String name) {
        return tableRow.get(name);
    }

    private boolean isTableFieldName(Map<String, String> tableRow, String name) {
        return tableRow.get(name) != null;
    }

    protected String getGroup(Matcher matcher, String name) {
        for (int i = 0; i < groupNames.length; i++) {
            String groupName = groupNames[i];
            if (name.equals(groupName)) {
                return getGroup(matcher, i);
            }
        }
        throw new NoGroupFoundForName(name, groupNames);
    }

	private String getGroup(Matcher matcher, int i) {
		return matcher.group(i + 1);
	}
    
    private boolean isGroupName(String name) {
        for (String groupName : groupNames) {
            if (name.equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    private int parameterPosition(String[] names, int position) {
        if (names.length == 0) {
            return -1;
        }
        String name = names[position];
        for (int i = 0; i < names.length; i++) {
            String annotatedName = names[i];
            if (annotatedName != null && name.equals(annotatedName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Extract annotated parameter names from the @Named parameter annotations
     * 
     * @return An array of annotated parameter names, which <b>may</b> include
     *         <code>null</code> values for parameters that are not annotated
     */
    protected String[] annotatedParameterNames() {
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

    private String startingWordFor(StepType stepType) {
        String startingWord = startingWordsByType.get(stepType);
        if (startingWord == null) {
            throw new StartingWordNotFound(stepType, startingWordsByType);
        }
        return startingWord;
    }

    protected Step createStep(final String stepAsString, Map<String, String> tableRow, Matcher matcher,
                              final Method method, final StepMonitor stepMonitor, String[] groupNames) {
        Type[] types = method.getGenericParameterTypes();
        String[] annotationNames = annotatedParameterNames();
        String[] parameterNames = paranamer.lookupParameterNames(method, false);
        final String translatedStep = translatedStep(stepAsString, tableRow, types, annotationNames, parameterNames);
        final Object[] args = argsForStep(tableRow, matcher, types, annotationNames, parameterNames);
        return new Step() {
            public StepResult perform() {
                try {
					stepMonitor.performing(stepAsString, dryRun);
					if (!dryRun) {
						method.invoke(stepsInstance, args);
					}
                    return StepResult.success(stepAsString).withTranslatedText(translatedStep);
                } catch (Throwable t) {
                    return failureWithOriginalException(stepAsString, t);
                }
            }

            private StepResult failureWithOriginalException(final String stepAsString, Throwable t) {
                if (t instanceof InvocationTargetException && t.getCause() != null) {
                    if (t.getCause() instanceof PendingError) {
                        return StepResult.pending(stepAsString, (PendingError) t.getCause());
                    } else {
                        return StepResult.failure(stepAsString, t.getCause());
                    }
                }
                return StepResult.failure(stepAsString, t);
            }

            public StepResult doNotPerform() {
                return StepResult.notPerformed(stepAsString);
            }

        };
    }

    public StepType getStepType() {
        return stepType;
    }

    public String getPatternAsString() {
        return patternAsString;
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return stepType + " " + patternAsString;
    }

    @SuppressWarnings("serial")
    public static class NoGroupFoundForName extends RuntimeException {

        public NoGroupFoundForName(String name, String[] groupNames) {
            super("No group found for name '" + name + "' amongst '" + asList(groupNames) + "'");
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