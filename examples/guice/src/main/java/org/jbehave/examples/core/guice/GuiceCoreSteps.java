package org.jbehave.examples.core.guice;

import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.TraderSteps;

import com.google.inject.Inject;

/**
 * POJO annotated to allow Guice injection.
 */
public class GuiceCoreSteps extends TraderSteps {

    @Inject
    public GuiceCoreSteps(TradingService service) {
        super(service);
    }

}
