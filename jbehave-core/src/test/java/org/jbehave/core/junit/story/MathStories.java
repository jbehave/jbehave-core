package org.jbehave.core.junit.story;

import java.util.Collections;
import java.util.List;

import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitReportingRunner;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.steps.ExampleSteps;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;

/**
 * <p>
 * {@link Embeddable} class to run multiple textual stories via JUnit.
 * </p>
 * <p>
 * Stories are specified in classpath and correspondingly the
 * {@link LoadFromClasspath} story loader is configured.
 * </p>
 */
@RunWith(JUnitReportingRunner.class)
public class MathStories extends JUnitStories {

    public MathStories() {
        useConfiguration(
                new MostUsefulConfiguration().useStoryReporterBuilder(new StoryReporterBuilder().withDefaultFormats()));
        JUnitReportingRunner.recommendedControls(configuredEmbedder());
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new ExampleSteps());
    }

    @Override
    public List<String> storyPaths() {
        return Collections.singletonList("org/jbehave/core/junit/story/Multiplication.story");
    }
}
