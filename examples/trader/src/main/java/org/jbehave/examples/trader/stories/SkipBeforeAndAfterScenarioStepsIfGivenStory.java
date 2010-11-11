package org.jbehave.examples.trader.stories;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.examples.trader.TraderStory;

public class SkipBeforeAndAfterScenarioStepsIfGivenStory extends TraderStory {

    @Override
    public Configuration configuration() {
        return super.configuration().useStoryControls(new StoryControls().doSkipBeforeAndAfterScenarioStepsIfGivenStory(true));
    }

}