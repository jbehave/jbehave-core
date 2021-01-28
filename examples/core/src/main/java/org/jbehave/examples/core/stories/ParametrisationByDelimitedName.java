package org.jbehave.examples.core.stories;

import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.examples.core.steps.ParametrisationByDelimitedNameSteps;

public class ParametrisationByDelimitedName extends JUnitStory {

    public ParametrisationByDelimitedName() {
        useConfiguration(new MostUsefulConfiguration()
                .useParameterControls(new ParameterControls().useDelimiterNamedParameters(true))
                .useStoryReporterBuilder(
                        new StoryReporterBuilder().withFormats(Format.CONSOLE)));
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new ParametrisationByDelimitedNameSteps());
    }

}