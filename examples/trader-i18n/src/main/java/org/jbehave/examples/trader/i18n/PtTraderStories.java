package org.jbehave.examples.trader.i18n;


public class PtTraderStories extends LocalizedTraderStories {
    
    @Override
    protected String language() {
        return "pt";
    }

    @Override
    protected Object traderSteps() {
        return new PtTraderSteps();
    }

    @Override
    protected String storyPattern() {
        return "**/pt_*.historia";
    }
 
}