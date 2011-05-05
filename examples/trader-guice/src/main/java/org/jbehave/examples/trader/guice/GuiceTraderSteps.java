package org.jbehave.examples.trader.guice;

import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.TraderSteps;

import com.google.inject.Inject;

/**
 * POJO annotated to allow Guice injection.
 */
public class GuiceTraderSteps extends TraderSteps {

    @Inject
    public GuiceTraderSteps(TradingService service) {
        super(service);
    }

}
