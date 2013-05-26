package org.jbehave.examples.core.weld;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jbehave.core.annotations.weld.WeldStep;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.TraderSteps;


/**
 * POJO annotated to allow Weld injection.
 */
@WeldStep @Singleton
public class WeldCoreSteps extends TraderSteps {

    @Inject
    public WeldCoreSteps(TradingService service) {
        super(service);
    }

}
