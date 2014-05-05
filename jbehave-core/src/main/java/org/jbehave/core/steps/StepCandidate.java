package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.Keywords.StartingWordNotFound;
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
    private final Class<?> stepsType;
    private final InjectableStepsFactory stepsFactory;
    private final Keywords keywords;
    private final StepMatcher stepMatcher;
    private final StepCreator stepCreator;
    private String[] composedSteps;
    private StepMonitor stepMonitor = new SilentStepMonitor();

    public StepCandidate(String patternAsString, int priority, StepType stepType, Method method, Class<?> stepsType,
            InjectableStepsFactory stepsFactory, Keywords keywords, StepPatternParser stepPatternParser,
            ParameterConverters parameterConverters, ParameterControls parameterControls) {
        this.patternAsString = patternAsString;
        this.priority = priority;
        this.stepType = stepType;
        this.method = method;
        this.stepsType = stepsType;
        this.stepsFactory = stepsFactory;
        this.keywords = keywords;
        this.stepMatcher = stepPatternParser.parseStep(stepType, patternAsString);
        this.stepCreator = new StepCreator(stepsType, stepsFactory, parameterConverters, parameterControls,
                stepMatcher, stepMonitor);
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
        return stepsFactory.createInstanceOfType(stepsType);
    }

    public Class<?> getStepsType() {
        return stepsType;
    }
    
    public StepType getStepType() {
        return stepType;
    }

    public String getStartingWord() {
        return keywords.startingWordFor(stepType);
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

    public String[] composedSteps() {
        return composedSteps;
    }

    public boolean ignore(String stepAsString) {
        try {
            String ignoreWord = keywords.startingWordFor(StepType.IGNORABLE);
            return keywords.stepStartsWithWord(stepAsString, ignoreWord);
        } catch (StartingWordNotFound e) {
            return false;
        }
    }

    public boolean isPending() {
        return method.isAnnotationPresent(Pending.class);
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
                    matchesType = keywords.startingWordFor(stepType).equals(findStartingWord(previousNonAndStep));
                }
            }
            stepMonitor.stepMatchesType(step, previousNonAndStep, matchesType, stepType, method, stepsType);
            boolean matchesPattern = stepMatcher.matches(stripStartingWord(step));
            stepMonitor.stepMatchesPattern(step, matchesPattern, stepMatcher.pattern(), method, stepsType);
            // must match both type and pattern
            return matchesType && matchesPattern;
        } catch (StartingWordNotFound e) {
            return false;
        }
    }

    public Step createMatchedStep(String stepAsString, Map<String, String> namedParameters) {
        return stepCreator.createParametrisedStep(method, stepAsString, stripStartingWord(stepAsString),
                namedParameters);
    }

    public Step createMatchedStepUponOutcome(String stepAsString, Map<String, String> namedParameters, Outcome outcome) {
        return stepCreator.createParametrisedStepUponOutcome(method, stepAsString, stripStartingWord(stepAsString),
                namedParameters, outcome);
    }

    public void addComposedSteps(List<Step> steps, String stepAsString, Map<String, String> namedParameters,
            List<StepCandidate> allCandidates) {
        addComposedStepsRecursively(steps, stepAsString, namedParameters, allCandidates, composedSteps);
    }

    private void addComposedStepsRecursively(List<Step> steps, String stepAsString,
            Map<String, String> namedParameters, List<StepCandidate> allCandidates, String[] composedSteps) {
        Map<String, String> matchedParameters = stepCreator.matchedParameters(method, stepAsString,
                stripStartingWord(stepAsString), namedParameters);
        matchedParameters.putAll(namedParameters);
        for (String composedStep : composedSteps) {
            addComposedStep(steps, composedStep, matchedParameters, allCandidates);
        }
    }

    private void addComposedStep(List<Step> steps, String composedStep, Map<String, String> matchedParameters,
            List<StepCandidate> allCandidates) {
        StepCandidate candidate = findComposedCandidate(composedStep, allCandidates);
        if (candidate != null) {
            steps.add(candidate.createMatchedStep(composedStep, matchedParameters));
            if (candidate.isComposite()) {
                // candidate is itself composite: recursively add composed steps
                addComposedStepsRecursively(steps, composedStep, matchedParameters, allCandidates,
                        candidate.composedSteps());
            }
        } else {
            steps.add(StepCreator.createPendingStep(composedStep, null));
        }
    }

    private StepCandidate findComposedCandidate(String composedStep, List<StepCandidate> allCandidates) {
        for (StepCandidate candidate : allCandidates) {
            if (StringUtils.startsWith(composedStep, candidate.getStartingWord())
                    && (StringUtils.endsWith(composedStep, candidate.getPatternAsString()) || candidate
                            .matches(composedStep))) {
                return candidate;
            }
        }
        return null;
    }

    public boolean isAndStep(String stepAsString) {
        return keywords.isAndStep(stepAsString);
    }

    public boolean isIgnorableStep(String stepAsString) {
        return keywords.isIgnorableStep(stepAsString);
    }

    private String findStartingWord(String stepAsString) {
        return keywords.startingWord(stepAsString, stepType);
    }

    private String stripStartingWord(String stepAsString) {
        return keywords.stepWithoutStartingWord(stepAsString, stepType);
    }

    @Override
    public String toString() {
        return stepType + " " + patternAsString;
    }

}