package org.jbehave.examples.core.guice;

import com.google.inject.Inject;

import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.TraderSteps;

/**
 * POJO annotated to allow Guice injection.
 */
public class GuiceCoreSteps extends TraderSteps {

    @Inject
    public GuiceCoreSteps(TradingService service) {
        super(service);
    }

}
