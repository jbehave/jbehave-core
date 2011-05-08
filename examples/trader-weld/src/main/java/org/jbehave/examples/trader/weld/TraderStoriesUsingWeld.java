package org.jbehave.examples.trader.weld;

import java.util.List;

import org.jbehave.core.configuration.weld.WeldBootstrap;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.weld.WeldStepsFactory;
import org.jbehave.examples.trader.TraderStories;
import org.jboss.weld.environment.se.WeldContainer;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;


/**
 * Run trader stories using WeldStepsFactory. The textual trader stories are
 * exactly the same ones found in the jbehave-trader-example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class TraderStoriesUsingWeld extends TraderStories {

    private static WeldContainer container;

    static {
        container = new WeldBootstrap().initialize();
    }
    
    @Override
    public InjectableStepsFactory stepsFactory() {
        return container.instance().select(WeldStepsFactory.class).get();
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../trader/src/main/java"), "**/*.story", "");
    }

}
