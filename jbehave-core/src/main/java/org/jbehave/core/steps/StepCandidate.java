package org.jbehave.core.steps;

import static java.text.MessageFormat.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.paranamer.Paranamer;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.Keywords.StartingWordNotFound;
import org.jbehave.core.parsers.StepMatcher;

/**
 * A StepCandidate is associated to a Java method annotated with {@link Given},
 * {@link When}, {@link Then} in a steps instance class. The StepCandidate is
 * responsible for matching the textual step against the pattern contained in
 * the method annotation via the {@link StepMatcher} and for the creation of the
 * matched executable step via the {@link StepCreator}.  The name of a StepCandidate
 * is the combination of its starting word and its pattern.
 */
public class StepCandidate {

    public static final String NAME_FORMAT = "{0} {1}";
    private final String patternAsString;
    private final Integer priority;
    private final StepType stepType;
    private final Method method;
    private final Class<?> stepsType;
    private final InjectableStepsFactory stepsFactory;
    private final Keywords keywords;
    private final StepMatcher stepMatcher;
    private final StepCreator stepCreator;
    private final String parameterPrefix;
    private final String[] composedSteps;
    private StepMonitor stepMonitor;

    public StepCandidate(String patternAsString, int priority, StepType stepType, Method method, Class<?> stepsType,
            InjectableStepsFactory stepsFactory, Keywords keywords, StepMatcher stepMatcher,
            String parameterPrefixString, StepCreator stepCreator, String[] steps, StepMonitor stepMonitor) {
        this.patternAsString = patternAsString;
        this.priority = priority;
        this.stepType = stepType;
        this.method = method;
        this.stepsType = stepsType;
        this.stepsFactory = stepsFactory;
        this.keywords = keywords;
        this.stepMatcher = stepMatcher;
        this.stepCreator = stepCreator;
        this.parameterPrefix = parameterPrefixString;
        this.composedSteps = steps;
        this.stepMonitor = stepMonitor;
    }

    public Method getMethod() {
        return method;
    }

    public String getName() {
        return format(NAME_FORMAT, getStartingWord(), getPatternAsString());
    }

    public Integer getPriority() {
        return priority;
    }

    public String getPatternAsString() {
        return patternAsString;
    }

    public Object getStepsInstance() {
        return stepsFactory != null ? stepsFactory.createInstanceOfType(stepsType) : null;
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

    public String getParameterPrefix() {
        return parameterPrefix;
    }

    public void useStepMonitor(StepMonitor stepMonitor) {
        this.stepMonitor = stepMonitor;
        this.stepCreator.useStepMonitor(stepMonitor);
    }

    public void useParanamer(Paranamer paranamer) {
        this.stepCreator.useParanamer(paranamer);
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
            return isIgnoredStep(stepAsString, ignoreWord);
        } catch (StartingWordNotFound e) {
            return false;
        }
    }

    public boolean comment(String stepAsString) {
        try {
            String ignoreWord = keywords.startingWordFor(StepType.IGNORABLE);
            return keywords.stepStartsWithWord(stepAsString, ignoreWord) && !isIgnoredStep(stepAsString, ignoreWord);
        } catch (StartingWordNotFound e) {
            return false;
        }
    }

    private boolean isIgnoredStep(String stepAsString, String ignoreWord) {
        for (Map.Entry<StepType, String> stepStartingWord : keywords.startingWordsByType().entrySet()) {
            if (stepStartingWord.getKey() != StepType.IGNORABLE) {
                if (keywords.stepStartsWithWords(stepAsString, ignoreWord, stepStartingWord.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPending() {
        return method != null && method.isAnnotationPresent(Pending.class);
    }

    public boolean matches(String stepAsString) {
        return matches(stepAsString, null);
    }

    public boolean matches(String step, String previousNonAndStep) {
        try {
            boolean matchesType = true;
            if (keywords.isAndStep(step)) {
                if (previousNonAndStep == null) {
                    // cannot handle AND step with no previous step
                    matchesType = false;
                } else {
                    // previous step type should match candidate step type
                    matchesType = stepType == keywords.stepTypeFor(previousNonAndStep);
                }
            }
            stepMonitor.stepMatchesType(step, previousNonAndStep, matchesType, stepType, method, stepsType);
            boolean matchesPattern = stepMatcher.matcher(stripStartingWord(step)).matches();
            stepMonitor.stepMatchesPattern(step, matchesPattern, stepMatcher.pattern(), method, stepsType);
            // must match both type and pattern
            return matchesType && matchesPattern;
        } catch (StartingWordNotFound e) {
            return false;
        }
    }

    public Step createMatchedStep(String stepAsString, Map<String, String> namedParameters, List<Step> composedSteps) {
        return stepCreator.createParametrisedStep(method, stepAsString, stripStartingWord(stepAsString),
                namedParameters, composedSteps);
    }

    public Step createMatchedStepUponOutcome(String stepAsString, Map<String, String> namedParameters,
            List<Step> composedSteps, Outcome outcome) {
        return stepCreator.createParametrisedStepUponOutcome(method, stepAsString, stripStartingWord(stepAsString),
                namedParameters, composedSteps, outcome);
    }

    public void addComposedSteps(List<Step> steps, String stepAsString, Map<String, String> namedParameters,
            List<StepCandidate> allCandidates) {
        Map<String, String> matchedParameters = stepCreator.matchedParameters(method,
                keywords.stepWithoutStartingWord(stepAsString), namedParameters);

        Map<String, String> mergedParameters = new HashMap<>(namedParameters);
        mergedParameters.putAll(matchedParameters);

        String previousNonAndStep = null;
        for (String composedStep : composedSteps) {
            addComposedStep(steps, composedStep, previousNonAndStep, mergedParameters, allCandidates);
            if (!(keywords.isAndStep(composedStep) || keywords.isIgnorableStep(composedStep))) {
                // only update previous step if not AND or IGNORABLE step
                previousNonAndStep = composedStep;
            }
        }
    }

    private void addComposedStep(List<Step> steps, String composedStep, String previousNonAndStep,
            Map<String, String> matchedParameters, List<StepCandidate> allCandidates) {
        if (ignore(composedStep)) {
            // ignorable steps are added so they can be reported
            steps.add(StepCreator.createIgnorableStep(composedStep));
        } else if (comment(composedStep)) {
            // comments are added so they can be reported
            steps.add(StepCreator.createComment(composedStep));
        } else {
            StepCandidate candidate = findComposedCandidate(composedStep, previousNonAndStep, allCandidates);
            if (candidate != null) {
                List<Step> composedSteps = new ArrayList<>();
                if (candidate.isComposite()) {
                    // candidate is itself composite: recursively add composed steps
                    candidate.addComposedSteps(composedSteps, composedStep, matchedParameters, allCandidates);
                }
                steps.add(candidate.createMatchedStep(composedStep, matchedParameters, composedSteps));
            } else {
                steps.add(StepCreator.createPendingStep(composedStep, previousNonAndStep));
            }
        }
    }

    private StepCandidate findComposedCandidate(String composedStep, String previousNonAndStep,
            List<StepCandidate> allCandidates) {
        StepType stepType;
        if (keywords.isAndStep(composedStep)) {
            if (previousNonAndStep != null) {
                stepType = keywords.stepTypeFor(previousNonAndStep);
            } else {
                // cannot handle AND step with no previous step
                return null;
            }
        } else {
            stepType = keywords.stepTypeFor(composedStep);
        }
        for (StepCandidate candidate : allCandidates) {
            if (stepType == candidate.getStepType() && (StringUtils.endsWith(composedStep,
                    candidate.getPatternAsString()) || candidate.matches(composedStep, previousNonAndStep))) {
                return candidate;
            }
        }
        return null;
    }

    protected String stripStartingWord(String stepAsString) {
        return keywords.stepWithoutStartingWord(stepAsString, stepType);
    }

    protected StepCreator getStepCreator() {
        return stepCreator;
    }

    protected Keywords getKeywords() {
        return keywords;
    }

    protected StepMatcher getStepMatcher() {
        return stepMatcher;
    }

    protected StepMonitor getStepMonitor() {
        return stepMonitor;
    }

    @Override
    public String toString() {
        return stepType + " " + patternAsString;
    }

}
