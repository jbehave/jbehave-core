package org.jbehave.examples.trader.spring;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.jbehave.examples.trader.TraderStories;
import org.springframework.context.ApplicationContext;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

/**
 * Run trader stories using SpringStepsFactory. The textual trader stories are
 * exactly the same ones found in the jbehave-trader-example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class TraderStoriesUsingSpring extends TraderStories {

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new SpringStepsFactory(configuration(), createContext());
    }

    private ApplicationContext createContext() {
        return new SpringApplicationContextFactory("org/jbehave/examples/trader/spring/steps.xml")
                .createApplicationContext();
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../trader/src/main/java"), "**/*.story", "");

    }

}
