package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.configuration.Configuration;

import static java.util.Arrays.asList;

/**
 * An {@link InjectableStepsFactory} that is provided Object instances.
 */
public class InstanceStepsFactory extends AbstractStepsFactory {

    private final Map<Class<?>,Object> stepsInstances = new LinkedHashMap<>();

    public InstanceStepsFactory(Configuration configuration, Object... stepsInstances) {
        this(configuration, asList(stepsInstances));
    }

    public InstanceStepsFactory(Configuration configuration, List<?> stepsInstances) {
        super(configuration);
        for (Object instance : stepsInstances) {
            this.stepsInstances.put(instance.getClass(), instance);
        }
    }

    @Override
    protected List<Class<?>> stepsTypes() {
        return new ArrayList<>(stepsInstances.keySet());
    }

    public Object createInstanceOfType(Class<?> type) {
        Object instance = stepsInstances.get(type);
        if ( instance == null ){
            throw new StepsInstanceNotFound(type, this);
        }
        return instance;
    }

}
