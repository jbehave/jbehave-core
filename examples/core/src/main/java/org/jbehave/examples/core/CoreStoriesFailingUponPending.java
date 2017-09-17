package org.jbehave.examples.core;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.StoryFinder;

/**
 */
public class CoreStoriesFailingUponPending extends CoreStories {

	public Configuration configuration() {
		return super.configuration()       		
					.usePendingStepStrategy(new FailingUponPendingStep());
	}

}