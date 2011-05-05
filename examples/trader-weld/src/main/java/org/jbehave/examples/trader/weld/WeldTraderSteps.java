package org.jbehave.examples.trader.weld;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jbehave.core.annotations.weld.WeldStep;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.TraderSteps;


/**
 * POJO annotated to allow Weld injection.
 */
@WeldStep @Singleton
public class WeldTraderSteps extends TraderSteps {

    @Inject
    public WeldTraderSteps(TradingService service) {
        super(service);
    }

}
