package org.jbehave.core.steps.guice;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * An {@link InjectableStepsFactory} that uses a Guice {@link Injector} for the
 * composition and instantiation of all components that contain JBehave
 * annotated methods.
 * 
 * @author Cristiano Gavião
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class GuiceStepsFactory extends AbstractStepsFactory {

    private final Injector injector;

    public GuiceStepsFactory(Configuration configuration, Injector injector) {
        super(configuration);
        this.injector = injector;
    }

    @Override
    protected List<Object> stepsInstances() {
        List<Object> steps = new ArrayList<Object>();
        addInstances(injector, steps);
        return steps;
    }

    /**
     * Adds steps instances from given injector and recursively its parent
     * 
     * @param injector the current Inject
     * @param steps the List of steps instances
     */
    private void addInstances(Injector injector, List<Object> steps) {
        for (Binding<?> binding : injector.getBindings().values()) {
            Key<?> key = binding.getKey();
            if (isAnnotated(key.getTypeLiteral().getType())) {
                steps.add(injector.getInstance(key));
            }
        }
        if (injector.getParent() != null) {
            addInstances(injector.getParent(), steps);
        }
    }
}
