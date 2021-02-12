package org.jbehave.core.steps.pico;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

/**
 * An {@link InjectableStepsFactory} that uses a {@link PicoContainer} for the
 * composition and instantiation of all components that contain JBehave
 * annotated methods.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class PicoStepsFactory extends AbstractStepsFactory {

    private final PicoContainer parent;

    public PicoStepsFactory(Configuration configuration, PicoContainer parent) {
        super(configuration);
        this.parent = parent;
    }

    @Override
    protected List<Class<?>> stepsTypes() {
        List<Class<?>> types = new ArrayList<>();
        for (ComponentAdapter<?> adapter : parent.getComponentAdapters()) {
            if (hasAnnotatedMethods(adapter.getComponentImplementation())) {
                types.add(adapter.getComponentImplementation());
            }
        }
        return types;
    }

    @Override
    public Object createInstanceOfType(Class<?> type) {
        Object instance = parent.getComponent(type);
        if ( instance == null ){
            throw new StepsInstanceNotFound(type, this);
        }
        return instance;
    }

}
