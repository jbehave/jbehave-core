package org.jbehave.examples.trader.stories;

import java.util.List;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.TraderStory;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;

public class AfterScenarioOutcome extends TraderStory {
    
    @Override
    public List<CandidateSteps> candidateSteps() {
        return new InstanceStepsFactory(configuration(), new SandpitSteps(), 
                new BeforeAfterSteps()).createCandidateSteps();
    }
        
}
