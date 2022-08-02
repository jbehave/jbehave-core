package org.jbehave.core.steps;

import java.lang.reflect.Method;

import org.jbehave.core.configuration.Configuration;

public abstract class AbstractCandidateSteps implements CandidateSteps {
    private final Configuration configuration;

    protected AbstractCandidateSteps(Configuration configuration) {
        this.configuration = configuration;
    }

    protected Configuration configuration() {
        return configuration;
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
}
