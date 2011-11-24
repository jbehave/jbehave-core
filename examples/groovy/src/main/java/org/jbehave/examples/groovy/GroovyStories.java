package org.jbehave.examples.groovy;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.ParanamerConfiguration;
import org.jbehave.core.configuration.groovy.GroovyContext;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.groovy.GroovyStepsFactory;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.ANSI_CONSOLE;

public class GroovyStories extends JUnitStories {

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
    protected List<String> storyPaths() {
        return new StoryFinder()
                .findPaths(codeLocationFromClass(this.getClass()), "**/*.story", "");
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        return new GroovyStepsFactory(configuration(), new GroovyContext()).createCandidateSteps();
    }

}
