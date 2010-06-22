package org.jbehave.examples.trader.spring;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.jbehave.examples.trader.TraderStory;
import org.springframework.context.ApplicationContext;

public abstract class TraderStoryUsingSpring extends TraderStory {

    @Override
    protected List<CandidateSteps> createSteps(Configuration configuration) {
        ApplicationContext context = new SpringApplicationContextFactory("org/jbehave/examples/trader/spring/steps.xml").createApplicationContext();
        return new SpringStepsFactory(configuration, context).createCandidateSteps();
    }

}
