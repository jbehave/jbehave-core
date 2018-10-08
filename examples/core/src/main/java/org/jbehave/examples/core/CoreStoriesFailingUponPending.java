package org.jbehave.examples.core;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.FailingUponPendingStep;

/**
 */
public class CoreStoriesFailingUponPending extends CoreStories {

	@Override
    public Configuration configuration() {
		return super.configuration()       		
					.usePendingStepStrategy(new FailingUponPendingStep());
	}

}
