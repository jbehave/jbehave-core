package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.List;

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
        List<CandidateSteps> steps = new ArrayList<CandidateSteps>();
        for (InjectableStepsFactory factory : stepsFactories) {
            steps.addAll(factory.createCandidateSteps());
        }
        return steps;
    }

}
