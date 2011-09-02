package org.jbehave.examples.trader.i18n;

import java.util.Locale;

import org.jbehave.examples.trader.i18n.steps.ItTraderSteps;


public class ItTraderStories extends LocalizedTraderStories {
    
    @Override
    protected Locale locale() {
        return new Locale("it");
    }

    @Override
    protected String storyPattern() {
        return "**/*.storia";
    }

    @Override
    protected Object traderSteps() {
        return new ItTraderSteps();
    }
 
}
