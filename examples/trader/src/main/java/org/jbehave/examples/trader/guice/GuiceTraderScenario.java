package org.jbehave.examples.trader.guice;

import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.TraderScenario;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.scenario.RunnableScenario;
import org.jbehave.scenario.steps.CandidateSteps;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.jbehave.scenario.steps.guice.GuiceStepsFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;

public class GuiceTraderScenario extends TraderScenario {

    public GuiceTraderScenario(Class<? extends RunnableScenario> scenarioClass) {
        super(scenarioClass);
    }

    @Override
    protected CandidateSteps[] createSteps(StepsConfiguration configuration) {
        Injector parent = createInjector();
        return new GuiceStepsFactory(configuration, parent).createCandidateSteps();
    }

    private Injector createInjector() {
        Injector parent = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
              bind(TradingService.class).in(Scopes.SINGLETON);
              bind(GuiceTraderSteps.class).in(Scopes.SINGLETON);
              bind(BeforeAfterSteps.class).in(Scopes.SINGLETON);
            }
          });
        return parent;
    }
    
}
