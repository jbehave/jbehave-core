package org.jbehave.examples.groovy;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.ParanamerConfiguration;
import org.jbehave.core.configuration.groovy.GroovyContext;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.*;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.groovy.GroovyStepsFactory;

import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.COLORED_CONSOLE;

public class GroovyStories extends JUnitStories {

    @Override
    public Configuration configuration() {
        return new ParanamerConfiguration()
                .useStoryReporterBuilder(new StoryReporterBuilder().withFormats(COLORED_CONSOLE));
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
