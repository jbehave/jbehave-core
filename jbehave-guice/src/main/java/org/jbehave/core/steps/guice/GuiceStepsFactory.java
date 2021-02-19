package org.jbehave.core.steps.guice;

import java.lang.reflect.Type;
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
    protected List<Class<?>> stepsTypes() {
        List<Class<?>> types = new ArrayList<>();
        addTypes(injector, types);
        return types;
    }

    /**
     * Adds steps types from given injector and recursively its parent
     * 
     * @param injector the current Inject
     * @param types the List of steps types
     */
    private void addTypes(Injector injector, List<Class<?>> types) {
        for (Binding<?> binding : injector.getBindings().values()) {
            Key<?> key = binding.getKey();
            Type type = key.getTypeLiteral().getType();
            if (hasAnnotatedMethods(type)) {
                types.add(((Class<?>)type));
            }
        }
        if (injector.getParent() != null) {
            addTypes(injector.getParent(), types);
        }
    }

    @Override
    public Object createInstanceOfType(Class<?> type) {
        List<Object> instances = new ArrayList<>();
        addInstances(injector, type, instances);
        if (!instances.isEmpty()) {
            return instances.iterator().next();
        }
        return new StepsInstanceNotFound(type, this);
    }

    private void addInstances(Injector injector, Class<?> type, List<Object> instances) {
        for (Binding<?> binding : injector.getBindings().values()) {
            Key<?> key = binding.getKey();
            if (type.equals(key.getTypeLiteral().getType())) {
                instances.add(injector.getInstance(key));
            }
        }
        if (injector.getParent() != null) {
            addInstances(injector.getParent(), type, instances);
        }
    }
}
