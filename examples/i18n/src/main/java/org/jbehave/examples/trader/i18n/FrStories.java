package org.jbehave.examples.trader.i18n;

import java.util.Locale;

import org.jbehave.core.junit.JUnitReportingRunner;
import org.jbehave.examples.trader.i18n.steps.FrSteps;
import org.junit.runner.RunWith;

@RunWith(JUnitReportingRunner.class)
public class FrStories extends LocalizedStories {
    
    @Override
    protected Locale locale() {
        return new Locale("fr");
    }

    @Override
    protected String storyPattern() {
        return "**/*.histoire";
    }

    @Override
    protected Object localizedSteps() {
        return new FrSteps();
    }

}
