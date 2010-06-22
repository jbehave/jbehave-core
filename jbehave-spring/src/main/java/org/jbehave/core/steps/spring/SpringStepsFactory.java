package org.jbehave.core.steps.spring;

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

    public SpringStepsFactory(Configuration configuration, String... resourceLocations) {
        super(configuration);
        this.context = new SpringApplicationContextFactory(resourceLocations).createApplicationContext();
    }

    public SpringStepsFactory(Configuration configuration, ApplicationContext context) {
        super(configuration);
        this.context = context;
    }

    @Override
    protected List<Object> stepsInstances() {
        List<Object> steps = new ArrayList<Object>();
        for (String name : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(name);
            if (isAnnotated(bean.getClass())) {
                steps.add(bean);
            }
        }
        return steps;
    }

}
