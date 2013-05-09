package org.jbehave.examples.trader.needle.provider;

import org.jbehave.core.annotations.needle.DefaultInstanceInjectionProvider;
import org.jbehave.examples.trader.service.TradingService;

/**
 * Injection provider holding the service.
 * 
 * @author Simon Zambrovski
 */
public final class TraderServiceInjectionProvider extends DefaultInstanceInjectionProvider<TradingService> {

  public TraderServiceInjectionProvider() {
    super(new TradingService());
  }
}
