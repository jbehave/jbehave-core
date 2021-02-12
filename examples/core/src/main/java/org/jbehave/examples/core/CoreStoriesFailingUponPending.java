package org.jbehave.examples.core;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.junit.JBehaveJUnit4Runner;
import org.junit.runner.RunWith;

/**
 */
@RunWith(JBehaveJUnit4Runner.class)
public class CoreStoriesFailingUponPending extends CoreStories {

    @Override
    public Configuration configuration() {
        return super.configuration()               
                    .usePendingStepStrategy(new FailingUponPendingStep());
    }

}
