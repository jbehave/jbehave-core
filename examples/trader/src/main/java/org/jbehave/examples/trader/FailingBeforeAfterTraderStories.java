package org.jbehave.examples.trader;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.steps.FailingBeforeAfterSteps;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

public class FailingBeforeAfterTraderStories extends TraderStories {

    @Override
    public List<CandidateSteps> candidateSteps() {
        List<CandidateSteps> candidateSteps = super.candidateSteps();
        candidateSteps.addAll(new InstanceStepsFactory(configuration(), new FailingBeforeAfterSteps()).createCandidateSteps());
        return candidateSteps;
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/failing_before*.story", "");
                
    }
}