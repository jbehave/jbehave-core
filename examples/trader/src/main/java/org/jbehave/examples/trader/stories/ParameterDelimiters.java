package org.jbehave.examples.trader.stories;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.examples.trader.TraderStory;

public class ParameterDelimiters extends TraderStory {

    @Override
    public Configuration configuration() {
        return super.configuration().useParameterControls(new ParameterControls().useNameDelimiterLeft("[").useNameDelimiterRight("]"));
    }
 
}
