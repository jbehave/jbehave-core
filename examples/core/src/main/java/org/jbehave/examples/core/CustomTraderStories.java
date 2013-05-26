package org.jbehave.examples.core;

import org.jbehave.core.configuration.Configuration;

public class CustomTraderStories extends CoreStories {

    @Override
    public Configuration configuration() {
        Configuration configuration = super.configuration();
        return configuration.useStoryReporterBuilder(configuration.storyReporterBuilder().withFormats(CustomHtmlOutput.FORMAT));
    }

}
