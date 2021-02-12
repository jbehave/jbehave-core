package org.jbehave.core.junit.story;

import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnit4StoryRunner;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.junit.steps.ExampleSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;

@RunWith(JUnit4StoryRunner.class)
public class FailedStepInScenarioJUnitStory extends JUnitStory {

    public FailedStepInScenarioJUnitStory() {
        useConfiguration(new MostUsefulConfiguration());
        JUnit4StoryRunner.recommendedControls(configuredEmbedder());

    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new ExampleSteps());
    }
}
