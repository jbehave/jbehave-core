package org.jbehave.core.steps;

import org.jbehave.core.steps.AbstractStepsFactory.StepsInstanceNotFound;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * An {@link InjectableStepsFactory} that is provided with the
 * {@link CandidateSteps} instances.
 */
public class ProvidedStepsFactory implements InjectableStepsFactory {

    private final List<CandidateSteps> candidateSteps;

    public ProvidedStepsFactory(){
        this(asList(new CandidateSteps[]{}));
    }

    public ProvidedStepsFactory(List<CandidateSteps> candidateSteps) {
        this.candidateSteps = candidateSteps;
    }

    @Override
    public List<CandidateSteps> createCandidateSteps() {
        return candidateSteps;
    }

    @Override
    public Object createInstanceOfType(Class<?> type) {
        Object instance = null;
        for (CandidateSteps steps : candidateSteps) {
            try {
                if (type.equals(((Steps) steps).type())) {
                    instance = ((Steps) steps).instance();
                }
            } catch (RuntimeException e) {
                // creation failed on given factory, carry on
            }
        }
        if (instance == null) {
            throw new StepsInstanceNotFound(type, this);
        }
        return instance;
    }

}
