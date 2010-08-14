package org.jbehave.examples.trader.i18n;

import java.util.Locale;

public class FrTraderStories extends LocalizedTraderStories {
    
    @Override
    protected Locale locale() {
        return new Locale("fr");
    }

    @Override
    protected String storyPattern() {
        return "**/*.histoire";
    }

    @Override
    protected Object traderSteps() {
        return new FrTraderSteps();
    }

}
