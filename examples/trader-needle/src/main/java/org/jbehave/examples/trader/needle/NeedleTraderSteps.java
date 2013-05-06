package org.jbehave.examples.trader.needle;

import javax.inject.Inject;

import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.TraderSteps;

/**
 * POJO annotated to allow Needle injection.
 */
public class NeedleTraderSteps extends TraderSteps {

	@Inject
	private TradingService injectedService;

	public NeedleTraderSteps() {

	}

	@Override
	public TradingService getService() {
		return this.injectedService;
	}

}
