package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.List;

import org.jbehave.core.configuration.Configuration;

/**
 * @author Valery Yatsynovich
 */
public abstract class AbstractCandidateSteps implements CandidateSteps {
    private final Configuration configuration;

    public AbstractCandidateSteps(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    protected void checkForDuplicateCandidates(List<StepCandidate> candidates, StepCandidate candidate) {
        String candidateName = candidateName(candidate);
        if (candidates.stream().anyMatch(c -> c.matches(candidateName) && candidate.matches(candidateName(c)))) {
            throw new DuplicateCandidateFound(candidate.getStepType(), candidate.getPatternAsString());
        }
    }

    private String candidateName(StepCandidate candidate) {
        return candidate.getStartingWord() + " " + candidate.getPatternAsString();
    }

    protected StepCandidate createCandidate(String stepPatternAsString, int priority, StepType stepType, Method method,
            Class<?> type, InjectableStepsFactory stepsFactory) {
        StepCandidate candidate = new StepCandidate(stepPatternAsString, priority, stepType, method, type,
                stepsFactory, configuration.stepsContext(), configuration.keywords(), configuration.stepPatternParser(),
                configuration.parameterConverters(), configuration.parameterControls());
        candidate.useStepMonitor(configuration.stepMonitor());
        candidate.useParanamer(configuration.paranamer());
        candidate.doDryRun(configuration.storyControls().dryRun());
        return candidate;
    }

    @SuppressWarnings("serial")
    public static class DuplicateCandidateFound extends RuntimeException {

        public DuplicateCandidateFound(StepType stepType, String patternAsString) {
            super(stepType + " " + patternAsString);
        }

    }
}
