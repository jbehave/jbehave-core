package org.jbehave.examples.trader.i18n;

import java.util.Locale;

import org.jbehave.examples.trader.i18n.steps.SvSteps;


public class SvStories extends LocalizedTraderStories {
    
    @Override
    protected Locale locale(){
        return new Locale("sv");
    }

    @Override
    protected String storyPattern() {
        return "**/*.story";
    }
 
    @Override
    protected Object traderSteps() {
        return new SvSteps();
    }

}