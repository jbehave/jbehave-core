package org.jbehave.examples.trader;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.steps.FailingBeforeAfterScenarioSteps;
import org.jbehave.examples.trader.steps.FailingBeforeAfterStoriesSteps;
import org.jbehave.examples.trader.steps.FailingBeforeAfterStorySteps;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

public class FailingBeforeTraderStories extends TraderStories {

    @Override
    public List<CandidateSteps> candidateSteps() {
        List<CandidateSteps> candidateSteps = super.candidateSteps();
        candidateSteps.addAll(new InstanceStepsFactory(configuration(), new FailingBeforeAfterScenarioSteps(),
                new FailingBeforeAfterStoriesSteps(), new FailingBeforeAfterStorySteps()).createCandidateSteps());
        return candidateSteps;
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/failing_before*.story", "");
                
    }
}