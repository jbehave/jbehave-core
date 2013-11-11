package org.jbehave.examples.trader.i18n;

import java.util.Locale;

import org.jbehave.examples.trader.i18n.steps.DeSteps;

public class DeStories extends LocalizedStories {
    
    @Override
    protected Locale locale() {
        return new Locale("de");
    }

    @Override
    protected String storyPattern() {
        return "**/*.geschichte";
    }

    @Override
    protected Object localizedSteps() {
        return new DeSteps();
    }

}
