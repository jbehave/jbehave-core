package org.jbehave.examples.trader.pico;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.pico.PicoStepsFactory;
import org.jbehave.examples.trader.TraderStories;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.AndSteps;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.CalendarSteps;
import org.jbehave.examples.trader.steps.PriorityMatchingSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;
import org.jbehave.examples.trader.steps.SearchSteps;
import org.jbehave.examples.trader.steps.TraderSteps;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;

/**
 * Run trader stories using PicoStepsFactory. The textual trader stories are
 * exactly the same ones found in the jbehave-trader-example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class TraderStoriesUsingPico extends TraderStories {

    @Override
    public List<CandidateSteps> candidateSteps() {
        return new PicoStepsFactory(configuration(), createPicoContainer()).createCandidateSteps();
    }

    private PicoContainer createPicoContainer() {
        MutablePicoContainer container = new DefaultPicoContainer(new Caching().wrap(new ConstructorInjection()));
        container.addComponent(TradingService.class);
        container.addComponent(TraderSteps.class);
        container.addComponent(BeforeAfterSteps.class);
        container.addComponent(AndSteps.class);
        container.addComponent(CalendarSteps.class);
        container.addComponent(PriorityMatchingSteps.class);
        container.addComponent(SandpitSteps.class);
        container.addComponent(SearchSteps.class);
        return container;
    }

    @Override
    protected List<String> storyPaths() {
        String searchInDirectory = codeLocationFromPath("../trader/src/main/java").getFile();
        return new StoryFinder().findPaths(searchInDirectory, asList("**/*.story"), null);
    }

}
