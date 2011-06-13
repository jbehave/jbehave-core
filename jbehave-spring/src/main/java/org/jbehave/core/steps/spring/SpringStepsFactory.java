package org.jbehave.core.steps.spring;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.springframework.context.ApplicationContext;

/**
 * An {@link InjectableStepsFactory} that uses Spring's
 * {@link ApplicationContext} for the composition and instantiation of all
 * components that contain JBehave annotated methods.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public class SpringStepsFactory extends AbstractStepsFactory {

    private final ApplicationContext context;

    public SpringStepsFactory(Configuration configuration, ApplicationContext context) {
        super(configuration);
        this.context = context;
    }

    @Override
    protected List<Class<?>> stepsTypes() {
        List<Class<?>> types = new ArrayList<Class<?>>();
        for (String name : context.getBeanDefinitionNames()) {
            Class<?> type = context.getType(name);
            if (isAllowed(type) && hasAnnotatedMethods(type)) {
                types.add(type);
            }
        }
        return types;
    }

    /**
     * Checks if type returned from context is allowed, i.e. not null and not
     * abstract.
     * 
     * @param type the Class of the bean
     * @return A boolean, <code>true</code> if allowed
     */
    protected boolean isAllowed(Class<?> type) {
        return type != null && !Modifier.isAbstract(type.getModifiers());
    }

    public Object createInstanceOfType(Class<?> type) {
        for (String name : context.getBeanDefinitionNames()) {
            Class<?> beanType = context.getType(name);
            if (type.equals(beanType)) {
                return context.getBean(name);
            }
        }

        throw new StepsInstanceNotFound(type, this);
    }

}
