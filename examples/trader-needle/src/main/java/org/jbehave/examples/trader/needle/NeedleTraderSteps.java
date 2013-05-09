package org.jbehave.examples.trader.needle;

import javax.inject.Inject;

import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.TraderSteps;

/**
 * POJO annotated to allow Needle injection.
 */
public class NeedleTraderSteps extends TraderSteps {

  @Inject
  public NeedleTraderSteps(final TradingService service) {
    super(service);
  }

}
