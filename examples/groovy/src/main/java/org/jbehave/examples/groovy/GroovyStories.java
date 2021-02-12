package org.jbehave.examples.groovy;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.ParanamerConfiguration;
import org.jbehave.core.configuration.groovy.GroovyContext;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.groovy.GroovyStepsFactory;
import org.jbehave.examples.core.CoreStories;

import java.util.List;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.ANSI_CONSOLE;

public class GroovyStories extends CoreStories {

    public GroovyStories() {
        // NOTE:  Will be overridden by any meta-filter set in command-line
        configuredEmbedder().useMetaFilters(asList("groovy: lang != 'java'"));
    }

    @Override
    public Configuration configuration() {
        return new ParanamerConfiguration()
                .useStoryReporterBuilder(new StoryReporterBuilder().withFormats(ANSI_CONSOLE));
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new GroovyStepsFactory(configuration(), new GroovyContext());
    }

    @Override
    public List<String> storyPaths() {
        String filter = System.getProperty("story.filter", "**/using_groovy.story");
        return findPaths(filter, "");
    }

    protected List<String> findPaths(String include, String exclude) {
        return new StoryFinder().findPaths(codeLocationFromClass(GroovyStories.class), include, exclude);
    }
}
