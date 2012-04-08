package org.jbehave.examples.trader.stories;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.examples.trader.TraderStory;

public class GivenStoriesParametrisedByExamples extends TraderStory {

    @Override
    public Configuration configuration() {
            return super.configuration()
                .useParameterControls(new ParameterControls().useDelimiterNamedParameters(true))
                .useStoryControls(new StoryControls().doParametriseGivenStoriesByExamples(true));
    }

}