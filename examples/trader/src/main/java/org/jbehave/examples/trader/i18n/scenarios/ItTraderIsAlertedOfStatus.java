package org.jbehave.examples.trader.i18n.scenarios;

import org.jbehave.examples.trader.i18n.ItTraderScenario;


public class ItTraderIsAlertedOfStatus extends ItTraderScenario {

    public ItTraderIsAlertedOfStatus() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ItTraderIsAlertedOfStatus(final ClassLoader classLoader) {
    	super(classLoader);
    }

}
