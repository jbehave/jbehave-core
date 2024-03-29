package org.jbehave.examples.core;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.junit.JUnit4StoryRunner;
import org.junit.runner.RunWith;

@RunWith(JUnit4StoryRunner.class)
public class CoreStoriesFailingUponPending extends CoreStories {

    @Override
    public Configuration configuration() {
        return super.configuration()               
                    .usePendingStepStrategy(new FailingUponPendingStep());
    }

}
