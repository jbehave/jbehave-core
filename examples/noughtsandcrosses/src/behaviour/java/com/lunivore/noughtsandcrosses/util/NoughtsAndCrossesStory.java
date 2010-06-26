package com.lunivore.noughtsandcrosses.util;

import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InstanceStepsFactory;

import com.lunivore.noughtsandcrosses.steps.BeforeAndAfterSteps;
import com.lunivore.noughtsandcrosses.steps.GridSteps;

public abstract class NoughtsAndCrossesStory extends JUnitStory {

	public NoughtsAndCrossesStory() {
        Configuration configuration = new MostUsefulConfiguration()
            .useStoryPathResolver(new UnderscoredCamelCaseResolver(""))
                .useStoryReporterBuilder(new StoryReporterBuilder()
                    .withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass()))
                    .withDefaultFormats()
                    .withFormats(CONSOLE, TXT)
                    .withFailureTrace(true));
        useConfiguration(configuration);
        OAndXUniverse universe = new OAndXUniverse();
        addSteps(new InstanceStepsFactory(configuration,new GridSteps(universe), new BeforeAndAfterSteps(universe)).createCandidateSteps());
     }

}
