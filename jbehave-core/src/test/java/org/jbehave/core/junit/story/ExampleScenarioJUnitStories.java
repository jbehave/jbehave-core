package org.jbehave.core.junit.story;

import java.util.Arrays;
import java.util.List;

import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnit4StoryRunner;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.steps.ExampleSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;

@RunWith(JUnit4StoryRunner.class)
public class ExampleScenarioJUnitStories extends JUnitStories {

    public ExampleScenarioJUnitStories() {
        useConfiguration(new MostUsefulConfiguration());
        JUnit4StoryRunner.recommendedControls(configuredEmbedder());
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new ExampleSteps());
    }

    @Override
    public List<String> storyPaths() {
        return Arrays.asList("org/jbehave/core/junit/story/Multiplication.story",
                "org/jbehave/core/junit/story/Empty.story");
    }

}
