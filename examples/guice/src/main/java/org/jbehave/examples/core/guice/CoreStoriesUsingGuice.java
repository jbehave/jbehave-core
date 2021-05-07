package org.jbehave.examples.core.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.guice.GuiceStepsFactory;
import org.jbehave.examples.core.CoreStories;
import org.jbehave.examples.core.guice.AnnotatedEmbedderUsingGuice.StepsModule;

/**
 * Run core stories using GuiceStepsFactory. The textual core stories are
 * exactly the same ones found in the core example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class CoreStoriesUsingGuice extends CoreStories {

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new GuiceStepsFactory(configuration(), createInjector());
    }

    private Injector createInjector() {
        return Guice.createInjector(new StepsModule());
    }

}
