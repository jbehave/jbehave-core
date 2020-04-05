package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.util.List;

import org.jbehave.core.configuration.Configuration;

import static java.text.MessageFormat.format;

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
        String candidateName = candidate.getName();
        if (candidates.stream().anyMatch(c -> c.matches(candidateName) && candidate.matches(c.getName()))) {
            throw new DuplicateCandidateFound(candidate);
        }
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
        public static final String DUPLICATE_FORMAT = "{0} {1}";

        public DuplicateCandidateFound(StepCandidate candidate) {
            super(format(DUPLICATE_FORMAT, candidate.getStepType(), candidate.getPatternAsString()));
        }

    }
}
