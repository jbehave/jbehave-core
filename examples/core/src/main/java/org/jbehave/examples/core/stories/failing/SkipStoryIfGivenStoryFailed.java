package org.jbehave.examples.core.stories.failing;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.examples.core.CoreStory;

public class SkipStoryIfGivenStoryFailed extends CoreStory {

    @Override
    public Configuration configuration() {
        return super.configuration().useStoryControls(new StoryControls().doSkipStoryIfGivenStoryFailed(true));
    }

}

