package org.jbehave.examples.trader.spring;

import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.jbehave.examples.trader.TraderStory;
import org.springframework.beans.factory.ListableBeanFactory;

public abstract class TraderStoryUsingSpring extends TraderStory {

    @Override
    protected CandidateSteps[] createSteps(StoryConfiguration configuration) {
        ListableBeanFactory parent = createBeanFactory();
        return new SpringStepsFactory(configuration, parent).createCandidateSteps();
    }

    private ListableBeanFactory createBeanFactory() {
        return new SpringApplicationContextFactory("org/jbehave/examples/trader/spring/steps.xml").getApplicationContext();
    }

}
