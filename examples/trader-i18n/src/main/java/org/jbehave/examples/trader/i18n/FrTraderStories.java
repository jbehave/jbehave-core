package org.jbehave.examples.trader.i18n;


public class FrTraderStories extends LocalizedTraderStories {
    
    @Override
    protected String language() {
        return "fr";
    }

    @Override
    protected Object traderSteps() {
        return new FrTraderSteps();
    }

    @Override
    protected String storyPattern() {
        return "**/fr_*.histoire";
    }
 
}
