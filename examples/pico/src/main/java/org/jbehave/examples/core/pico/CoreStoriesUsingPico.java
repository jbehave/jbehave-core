package org.jbehave.examples.core.pico;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.pico.PicoStepsFactory;
import org.jbehave.examples.core.CoreStories;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;
import org.jbehave.examples.core.steps.TraderSteps;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

/**
 * Run core stories using PicoStepsFactory. The textual trader stories are
 * exactly the same ones found in the core example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class CoreStoriesUsingPico extends CoreStories {

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new PicoStepsFactory(configuration(), createPicoContainer());
    }

    private PicoContainer createPicoContainer() {
        MutablePicoContainer container = new DefaultPicoContainer(new Caching().wrap(new ConstructorInjection()));
        container.addComponent(TradingService.class);
        container.addComponent(TraderSteps.class);
        container.addComponent(BeforeAfterSteps.class);
        container.addComponent(AndSteps.class);
        container.addComponent(CalendarSteps.class);
        container.addComponent(PendingSteps.class);
        container.addComponent(PriorityMatchingSteps.class);
        container.addComponent(SandpitSteps.class);
        container.addComponent(SearchSteps.class);
        return container;
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }

}
