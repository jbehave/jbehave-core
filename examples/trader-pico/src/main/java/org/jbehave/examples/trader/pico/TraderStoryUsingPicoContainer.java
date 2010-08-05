package org.jbehave.examples.trader.pico;

import java.util.List;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.pico.PicoStepsFactory;
import org.jbehave.examples.trader.TraderStory;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.TraderSteps;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;

/**
 * Example of configuring a single story to use steps defined in a Pico container.
 */
public abstract class TraderStoryUsingPicoContainer extends TraderStory {

    @Override
    public List<CandidateSteps> candidateSteps(){
        return new PicoStepsFactory(configuration(), createPicoContainer()).createCandidateSteps();
    }

    private PicoContainer createPicoContainer() {
        MutablePicoContainer parent = new DefaultPicoContainer(new Caching().wrap(new ConstructorInjection()));
        parent.as(Characteristics.USE_NAMES).addComponent(TradingService.class);
        parent.as(Characteristics.USE_NAMES).addComponent(TraderSteps.class);
        parent.as(Characteristics.USE_NAMES).addComponent(BeforeAfterSteps.class);
        return parent;
    }
    
}
