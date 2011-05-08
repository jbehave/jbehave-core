package org.jbehave.examples.trader.guice;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.guice.GuiceStepsFactory;
import org.jbehave.examples.trader.TraderStories;
import org.jbehave.examples.trader.guice.AnnotatedEmbedderUsingGuice.StepsModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

/**
 * Run trader stories using GuiceStepsFactory. The textual trader stories are
 * exactly the same ones found in the jbehave-trader-example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class TraderStoriesUsingGuice extends TraderStories {

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new GuiceStepsFactory(configuration(), createInjector());
    }

    private Injector createInjector() {
        return Guice.createInjector(new StepsModule());
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../trader/src/main/java"), "**/*.story", "");
    }

}
