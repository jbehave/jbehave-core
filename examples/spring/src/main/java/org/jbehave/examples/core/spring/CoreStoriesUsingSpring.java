package org.jbehave.examples.core.spring;

import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.jbehave.examples.core.CoreStories;
import org.springframework.context.ApplicationContext;

/**
 * Run core stories using SpringStepsFactory. The textual core stories are
 * exactly the same ones found in the core example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class CoreStoriesUsingSpring extends CoreStories {

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new SpringStepsFactory(configuration(), createContext());
    }

    protected ApplicationContext createContext() {
        return new SpringApplicationContextFactory("org/jbehave/examples/core/spring/steps.xml")
                .createApplicationContext();
    }

}
