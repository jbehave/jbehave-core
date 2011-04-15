package org.jbehave.examples.trader.stories;

import java.util.List;

import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.TraderStory;

public class FailingBeforeStories extends TraderStory {

    @Override
    public Configuration configuration() {
        return super.configuration().useStoryControls(new StoryControls().doResetStateBeforeScenario(false));
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        List<CandidateSteps> candidateSteps = super.candidateSteps();
        candidateSteps.addAll(new InstanceStepsFactory(configuration(), this).createCandidateSteps());
        return candidateSteps;
    }

    @BeforeStories
    public void beforeStories(){
        throw new RuntimeException("Bum go the stories");
    }
}
