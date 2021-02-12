package org.jbehave.example.spring.security;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnit4StoryRunner;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

import static org.jbehave.core.reporters.Format.ANSI_CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;

@RunWith(JUnit4StoryRunner.class)
public class SpringSecurityStories extends JUnitStories {

    private ConfigurableApplicationContext context;

    public SpringSecurityStories() {
        configuredEmbedder().embedderControls().doGenerateViewAfterStories(true).doIgnoreFailureInStories(false)
                .doIgnoreFailureInView(true).doVerboseFailures(true);
    }

    @Override
    public Configuration configuration() {
        Configuration configuration = new MostUsefulConfiguration();
        configuration.useStoryReporterBuilder(
                new StoryReporterBuilder()
                        .withCodeLocation(CodeLocations.codeLocationFromClass(getClass()))
                        .withDefaultFormats()
                        .withFormats(ANSI_CONSOLE, HTML));
        return configuration;
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new SpringStepsFactory(configuration(), context());
    }

    private ApplicationContext context() {
        if (context == null) {
            context = new SpringApplicationContextFactory("steps.xml").createApplicationContext();
        }
        return context;
    }

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(CodeLocations.codeLocationFromClass(getClass()), "**/*.story", "");

    }

}
