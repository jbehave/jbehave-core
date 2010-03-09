package org.jbehave.examples.trader.pico;

import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.TraderScenario;
import org.jbehave.examples.trader.TraderSteps;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.scenario.RunnableScenario;
import org.jbehave.scenario.steps.CandidateSteps;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.jbehave.scenario.steps.pico.PicoStepsFactory;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjection;

public class PicoTraderScenario extends TraderScenario {

    public PicoTraderScenario(Class<? extends RunnableScenario> scenarioClass) {
        super(scenarioClass);
    }

    @Override
    protected CandidateSteps[] createSteps(StepsConfiguration configuration) {
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
