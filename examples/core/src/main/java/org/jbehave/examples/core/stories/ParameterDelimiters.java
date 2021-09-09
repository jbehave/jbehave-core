package org.jbehave.examples.core.stories;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.examples.core.CoreStory;

public class ParameterDelimiters extends CoreStory {

    @Override
    public Configuration configuration() {
        return super.configuration().useParameterControls(
                new ParameterControls().useNameDelimiterLeft("[").useNameDelimiterRight("]"));
    }
 
}
