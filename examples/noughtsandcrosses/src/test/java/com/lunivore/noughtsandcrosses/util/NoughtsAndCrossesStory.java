package com.lunivore.noughtsandcrosses.util;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InstanceStepsFactory;

import com.lunivore.noughtsandcrosses.steps.BeforeAndAfterSteps;
import com.lunivore.noughtsandcrosses.steps.GridSteps;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.TXT;

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
        WindowControl windowControl = new WindowControl();
        addSteps(new InstanceStepsFactory(configuration,new GridSteps(windowControl), new BeforeAndAfterSteps(windowControl)).createCandidateSteps());
     }

}
