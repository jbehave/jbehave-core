package org.jbehave.examples.trader;

import java.util.List;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.steps.FailingBeforeAfterSteps;

public abstract class FailingBeforeAfterTraderStory extends TraderStory {

    @Override
    public List<CandidateSteps> candidateSteps() {
        List<CandidateSteps> candidateSteps = super.candidateSteps();
        candidateSteps.addAll(new InstanceStepsFactory(configuration(), new FailingBeforeAfterSteps()).createCandidateSteps());
        return candidateSteps;
    }

}
