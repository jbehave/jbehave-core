package org.jbehave.examples.trader.stories;

import java.util.List;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.TraderStory;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.FailingBeforeAfterStoriesSteps;
import org.jbehave.examples.trader.steps.TraderSteps;

public class FailingBeforeStories extends TraderStory {
    @Override
    public List<CandidateSteps> candidateSteps() {
        return new InstanceStepsFactory(configuration(), new TraderSteps(new TradingService()), 
                new FailingBeforeAfterStoriesSteps()).createCandidateSteps();
    }
        
}
