package org.jbehave.examples.trader.pico;

import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.pico.PicoStepsFactory;
import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.TraderSteps;
import org.jbehave.examples.trader.TraderStory;
import org.jbehave.examples.trader.service.TradingService;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;

public abstract class TraderStoryUsingPicoContainer extends TraderStory {

    @Override
    protected CandidateSteps[] createSteps(StoryConfiguration configuration) {
        PicoContainer parent = createPicoContainer();
        return new PicoStepsFactory(configuration, parent).createCandidateSteps();
    }

    private PicoContainer createPicoContainer() {
        MutablePicoContainer parent = new DefaultPicoContainer(new Caching().wrap(new ConstructorInjection()));
        parent.as(Characteristics.USE_NAMES).addComponent(TradingService.class);
        parent.as(Characteristics.USE_NAMES).addComponent(TraderSteps.class);
        parent.as(Characteristics.USE_NAMES).addComponent(BeforeAfterSteps.class);
        return parent;
    }
    
}
