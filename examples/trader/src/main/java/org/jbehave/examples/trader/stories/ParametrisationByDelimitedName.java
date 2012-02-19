package org.jbehave.examples.trader.stories;

import java.util.List;

import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.steps.ParametrisationByDelimitedNameSteps;

public class ParametrisationByDelimitedName extends JUnitStory {

    public ParametrisationByDelimitedName() {
        useConfiguration(new MostUsefulConfiguration()
                .useStoryReporterBuilder(
                        new StoryReporterBuilder().withFormats(Format.CONSOLE)));
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        return new InstanceStepsFactory(configuration(), new ParametrisationByDelimitedNameSteps())
                .createCandidateSteps();
    }

}