package org.jbehave.core.steps;

import java.util.List;

import org.jbehave.core.configuration.Configuration;

/**
 * An {@link InjectableStepsFactory} that is provided with the {@link Configuration}
 * and {@link CandidateSteps} instances.
 */
public class ProvidedStepsFactory implements InjectableStepsFactory {

    private final Configuration configuration;
    private final List<CandidateSteps> candidateSteps;

    public ProvidedStepsFactory(Configuration configuration, List<CandidateSteps> candidateSteps) {
        this.configuration = configuration;
        this.candidateSteps = candidateSteps;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public List<CandidateSteps> createCandidateSteps() {
        return candidateSteps;
    }

}
