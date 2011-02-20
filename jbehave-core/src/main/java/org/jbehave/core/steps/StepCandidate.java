package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.parsers.StepPatternParser;

import com.thoughtworks.paranamer.Paranamer;

/**
 * A StepCandidate is associated to a Java method annotated with {@link Given},
 * {@link When}, {@link Then} in a steps instance class. The StepCandidate is
 * responsible for matching the textual step against the pattern contained in
 * the method annotation via the {@link StepMatcher} and for the creation of the
 * matched executable step via the {@link StepCreator}.
 */
public class StepCandidate {

    private final String patternAsString;
    private final Integer priority;
    private final StepType stepType;
    private final Method method;
    private final Object stepsInstance;
    private final Map<StepType, String> startingWordsByType;
    private final StepMatcher stepMatcher;
    private final StepCreator stepCreator;
    private StepMonitor stepMonitor = new SilentStepMonitor();
    private String[] composedSteps;

    public StepCandidate(String patternAsString, int priority, StepType stepType, Method method, Object stepsInstance,
            Map<StepType, String> startingWordsByType, StepPatternParser stepPatternParser,
            ParameterConverters parameterConverters) {
        this.patternAsString = patternAsString;
        this.priority = priority;
        this.stepType = stepType;
        this.method = method;
        this.stepsInstance = stepsInstance;
        this.startingWordsByType = startingWordsByType;
        this.stepMatcher = stepPatternParser.parseStep(stepType, patternAsString);
        this.stepCreator = new StepCreator(stepsInstance, parameterConverters, stepMatcher, stepMonitor);
    }

    public Method getMethod() {
        return method;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getPatternAsString() {
        return patternAsString;
    }

    public Object getStepsInstance() {
        return stepsInstance;
    }

    public StepType getStepType() {
        return stepType;
    }

    public String getStartingWord() {
        return startingWordFor(stepType);
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

    public void composedOf(String[] steps) {
        this.composedSteps = steps;        
    }
    
    public boolean isComposite() {
        return composedSteps != null && composedSteps.length > 0;
    }

    public boolean ignore(String stepAsString) {
        try {
            String ignoreWord = startingWordFor(StepType.IGNORABLE);
            return stepStartsWithWord(stepAsString, ignoreWord);
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
            if (isAndStep(step)) {
                if (previousNonAndStep == null) {
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
        String andWord = startingWordFor(StepType.AND);
        return stepStartsWithWord(stepAsString, andWord);
    }

    public Step createMatchedStep(String stepAsString, Map<String, String> namedParameters) {
        return stepCreator.createParametrisedStep(method, stepAsString, stripStartingWord(stepAsString), namedParameters);
    }
    
    public List<Step> createComposedSteps(String stepAsString, Map<String, String> namedParameters,
            List<StepCandidate> allCandidates) {
        Map<String, String> matchedParameters;
        if ( namedParameters.isEmpty() ){
            matchedParameters = stepCreator.matchedParameters(method, stepAsString, stripStartingWord(stepAsString), namedParameters);
        } else {
            matchedParameters = namedParameters;
        }
        List<Step> steps = new ArrayList<Step>();
        for (String composedStep : composedSteps) {
            StepCandidate candidate = findComposedCandidate(composedStep, allCandidates);
            if ( candidate != null ){
                steps.add(candidate.createMatchedStep(composedStep, matchedParameters));
            }
        }
        return steps;
    }

    private StepCandidate findComposedCandidate(String composedStep, List<StepCandidate> allCandidates) {
        for (StepCandidate candidate : allCandidates) {
            if ( StringUtils.startsWith(composedStep, candidate.getStartingWord()) &&
                 StringUtils.endsWith(composedStep, candidate.getPatternAsString()) ){
                return candidate;
            }
        }
        return null;
    }

    private String stripStartingWord(final String stepAsString) {
        String startingWord = findStartingWord(stepAsString);
        return trimStartingWord(startingWord, stepAsString);
    }

    private String findStartingWord(final String stepAsString) throws StartingWordNotFound {
        String wordForType = startingWordFor(stepType);
        if (stepStartsWithWord(stepAsString, wordForType)) {
            return wordForType;
        }
        String andWord = startingWordFor(StepType.AND);
        if (stepStartsWithWord(stepAsString, andWord)) {
            return andWord;
        }
        throw new StartingWordNotFound(stepAsString, stepType, startingWordsByType);
    }

    private boolean stepStartsWithWord(String step, String word) {
        return step.startsWith(word + " "); // space after qualifies it as word
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
                    + startingWordsByType + "'");
        }

        public StartingWordNotFound(StepType stepType, Map<StepType, String> startingWordsByType) {
            super("No starting word found of type '" + stepType + "' amongst '" + startingWordsByType + "'");
        }

    }


}