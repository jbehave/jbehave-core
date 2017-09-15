package org.jbehave.examples.core.stories.failing;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.examples.core.CoreStory;

public class ExamplesTableFailing extends CoreStory {

    @Override
    public Configuration configuration() {
        Configuration configuration = super.configuration();
        configuration.storyControls().doResetStateBeforeScenario(false);
        return configuration;
    }

}
