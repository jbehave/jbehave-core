package org.jbehave.examples.trader;

import org.jbehave.core.configuration.Configuration;

public class CustomTraderStories extends TraderStories {

    @Override
    public Configuration configuration() {
        Configuration configuration = super.configuration();
        return configuration.useStoryReporterBuilder(configuration.storyReporterBuilder().withFormats(CustomHtmlOutput.FORMAT));
    }

}
