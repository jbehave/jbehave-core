package org.jbehave.examples.trader.i18n;


public class ItTraderStories extends I18nTraderStories {
    
    @Override
    protected String language() {
        return "it";
    }

    @Override
    protected Object traderSteps() {
        return new ItTraderSteps();
    }

    @Override
    protected String storyPattern() {
        return "**/it_*.storia";
    }
 
}
