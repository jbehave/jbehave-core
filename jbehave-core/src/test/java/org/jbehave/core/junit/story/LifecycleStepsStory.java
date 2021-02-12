package org.jbehave.core.junit.story;

import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JBehaveJUnit4Runner;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.junit.steps.ExampleSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;

@RunWith(JBehaveJUnit4Runner.class)
public class LifecycleStepsStory extends JUnitStory {

    public LifecycleStepsStory() {
        useConfiguration(new MostUsefulConfiguration());
        JBehaveJUnit4Runner.recommendedControls(configuredEmbedder());
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new ExampleSteps());
    }
}
