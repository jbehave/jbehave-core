package org.jbehave.examples.core.guice;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.guice.GuiceStepsFactory;
import org.jbehave.examples.core.CoreStories;
import org.jbehave.examples.core.guice.AnnotatedEmbedderUsingGuice.StepsModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

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

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }

}
