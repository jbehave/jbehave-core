package org.jbehave.examples.core.needle.provider;

import org.jbehave.examples.core.service.TradingService;

import de.holisticon.toolbox.needle.provider.DefaultInstanceInjectionProvider;

/**
 * Injection provider holding the service.
 * 
 * @author Simon Zambrovski
 */
public final class TraderServiceInjectionProvider extends
		DefaultInstanceInjectionProvider<TradingService> {

	public TraderServiceInjectionProvider() {
		super(new TradingService());
	}
}
