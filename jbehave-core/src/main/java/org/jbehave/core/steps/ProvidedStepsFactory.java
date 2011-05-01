package org.jbehave.core.steps;

import java.util.List;

/**
 * An {@link InjectableStepsFactory} that is provided with the
 * {@link CandidateSteps} instances.
 */
public class ProvidedStepsFactory implements InjectableStepsFactory {

    private final List<CandidateSteps> candidateSteps;

    public ProvidedStepsFactory(List<CandidateSteps> candidateSteps) {
        this.candidateSteps = candidateSteps;
    }

    public List<CandidateSteps> createCandidateSteps() {
        return candidateSteps;
    }

}
