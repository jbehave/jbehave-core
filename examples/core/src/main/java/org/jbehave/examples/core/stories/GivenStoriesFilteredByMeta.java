package org.jbehave.examples.core.stories;

import java.util.Arrays;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.core.CoreStory;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.TraderSteps;

public class GivenStoriesFilteredByMeta extends CoreStory {

	public GivenStoriesFilteredByMeta() {
		configuredEmbedder().useMetaFilters(Arrays.asList("+run"));
	}

	@Override
    public Configuration configuration() {
		return super.configuration().useStoryControls(
				new StoryControls().doIgnoreMetaFiltersIfGivenStory(true));
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new TraderSteps(
				new TradingService()));
	}

}
