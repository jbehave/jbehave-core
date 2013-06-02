package org.jbehave.examples.core.needle.steps;

import javax.inject.Inject;

import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.TraderSteps;

/**
 * POJO annotated to allow Needle injection.
 */
public class NeedleTraderSteps extends TraderSteps {

  @Inject
  public NeedleTraderSteps(final TradingService service) {
    super(service);
  }

}
