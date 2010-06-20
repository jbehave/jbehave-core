package org.jbehave.core.steps;

import static java.util.Arrays.asList;

import java.util.List;

import org.jbehave.core.configuration.Configuration;

/**
 * An {@link InjectableStepsFactory} that is provided Object instances.
 */
public class InstanceStepsFactory extends AbstractStepsFactory {

    private final List<Object> stepsInstances;

    public InstanceStepsFactory(Configuration configuration, Object... stepsInstances) {
        this(configuration, asList(stepsInstances));
    }

    public InstanceStepsFactory(Configuration configuration, List<Object> stepsInstances) {
        super(configuration);
        this.stepsInstances = stepsInstances;
    }

    @Override
    protected List<Object> stepsInstances() {
        return stepsInstances;
    }

}
