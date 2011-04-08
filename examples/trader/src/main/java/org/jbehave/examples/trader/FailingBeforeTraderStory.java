package org.jbehave.examples.trader;

import java.util.List;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.steps.FailingBeforeAfterScenarioSteps;
import org.jbehave.examples.trader.steps.FailingBeforeAfterStoriesSteps;
import org.jbehave.examples.trader.steps.FailingBeforeAfterStorySteps;

public abstract class FailingBeforeTraderStory extends TraderStory {

    @Override
    public List<CandidateSteps> candidateSteps() {
        List<CandidateSteps> candidateSteps = super.candidateSteps();
        candidateSteps.addAll(new InstanceStepsFactory(configuration(), new FailingBeforeAfterScenarioSteps(),
                new FailingBeforeAfterStoriesSteps(), new FailingBeforeAfterStorySteps()).createCandidateSteps());
        return candidateSteps;
    }

}
