package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.Map;

import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.parsers.StepPatternParser;

import com.thoughtworks.paranamer.Paranamer;

/**
 * A candidate step is associated to a Java method annotated with {@link Given},
 * {@link When}, {@link Then} in a {@link CandidatesSteps} instance class.
 * The candidate step is responsible for matching the textual step against the
 * pattern contained in the method annotation via the {@link StepMatcher} and
 * for the creation of the matched executable step via the {@link StepCreator}.
 */
public class CandidateStep {

    private final String patternAsString;
    private final Integer priority;
    private final StepType stepType;
    private final Method method;
    private final Object stepsInstance;
    private final Map<StepType, String> startingWordsByType;
	private final StepMatcher stepMatcher;
	private final StepCreator stepCreator;
    private StepMonitor stepMonitor = new SilentStepMonitor();
    
    public CandidateStep(String patternAsString, int priority, StepType stepType, Method method,
            Object stepsInstance, StepPatternParser stepPatternParser,
            ParameterConverters parameterConverters, Map<StepType, String> startingWords) {
        this.patternAsString = patternAsString;
        this.priority = priority;
        this.stepType = stepType;
        this.method = method;
        this.stepsInstance = stepsInstance;
        this.startingWordsByType = startingWords;
        this.stepMatcher = stepPatternParser.parseStep(patternAsString);
        this.stepCreator = new StepCreator(stepsInstance, parameterConverters, stepMatcher, stepMonitor);
    }

	public Method getMethod() {
		return method;
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

    public void useStepMonitor(StepMonitor stepMonitor) {
        this.stepMonitor = stepMonitor;
        this.stepCreator.useStepMonitor(stepMonitor);
    }

	public void doDryRun(boolean dryRun) {
		this.stepCreator.doDryRun(dryRun);
	}

    public void useParanamer(Paranamer paranamer) {
        this.stepCreator.useParanamer(paranamer);
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
        			// cannot handle AND step with no previous step
        			matchesType = false; 
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

    public Step createMatchedStep(String stepAsString, Map<String, String> tableRow) {
        stepMatcher.find(stripStartingWord(stepAsString));
        return stepCreator.createParametrisedStep(method, stepAsString, tableRow);
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


    @Override
    public String toString() {
        return stepType + " " + patternAsString;
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

    
}