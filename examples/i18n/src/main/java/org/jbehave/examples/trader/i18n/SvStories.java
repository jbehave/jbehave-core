package org.jbehave.examples.trader.i18n;

import java.util.Locale;

import org.jbehave.core.junit.JBehaveJUnit4Runner;
import org.jbehave.examples.trader.i18n.steps.SvSteps;
import org.junit.runner.RunWith;

@RunWith(JBehaveJUnit4Runner.class)
public class SvStories extends LocalizedStories {
    
    @Override
    protected Locale locale(){
        return new Locale("sv");
    }

    @Override
    protected String storyPattern() {
        return "**/*.story";
    }
 
    @Override
    protected Object localizedSteps() {
        return new SvSteps();
    }

}