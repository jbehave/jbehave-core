package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.steps.AbstractStepsFactory.StepsInstanceNotFound;

/**
 * An {@link InjectableStepsFactory} that composes {@link CandidateSteps}
 * created from other factories.
 */
public class CompositeStepsFactory implements InjectableStepsFactory {

    private final InjectableStepsFactory[] stepsFactories;

    public CompositeStepsFactory(InjectableStepsFactory... stepsFactories) {
        this.stepsFactories = stepsFactories;
    }

    public List<CandidateSteps> createCandidateSteps() {
        List<CandidateSteps> steps = new ArrayList<>();
        for (InjectableStepsFactory factory : stepsFactories) {
            steps.addAll(factory.createCandidateSteps());
        }
        return steps;
    }

    public Object createInstanceOfType(Class<?> type) {
        Object instance = null;
        for (InjectableStepsFactory factory : stepsFactories) {
            try {
                instance = factory.createInstanceOfType(type);
            } catch (RuntimeException e) {
                // creation failed on given factory, carry on
            }
        }
        if ( instance == null ){
            throw new StepsInstanceNotFound(type, this);
        }
        return instance;
    }

}
