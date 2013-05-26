package org.jbehave.examples.trader.i18n;

import java.util.Locale;

import org.jbehave.examples.trader.i18n.steps.PtSteps;


public class PtStories extends LocalizedStories {
    
    @Override
    protected Locale locale(){
        return new Locale("pt");
    }

    @Override
    protected String storyPattern() {
        return "**/*.historia";
    }
 
    @Override
    protected Object localizedSteps() {
        return new PtSteps();
    }

}