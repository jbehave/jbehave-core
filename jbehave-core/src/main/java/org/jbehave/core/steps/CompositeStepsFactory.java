package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;

/**
 * An {@link InjectableStepsFactory} that is composes {@link CandidateSteps} from several other factories,
 * all using the same {@link Configuration}.
 */
public class CompositeStepsFactory implements InjectableStepsFactory {

    private final Configuration configuration;
    private final InjectableStepsFactory[] stepsFactories;

    public CompositeStepsFactory(Configuration configuration, InjectableStepsFactory... stepsFactories) {
        this.configuration = configuration;
        this.stepsFactories = stepsFactories;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public List<CandidateSteps> createCandidateSteps() {
        List<CandidateSteps> steps = new ArrayList<CandidateSteps>();
        for (InjectableStepsFactory factory : stepsFactories) {
            steps.addAll(factory.createCandidateSteps());
        }
        return steps;
    }

}
