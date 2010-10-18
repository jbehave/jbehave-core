package org.jbehave.examples.groovy;

import org.jbehave.paranamer.BytecodeReadingParanamer;
import org.jbehave.paranamer.CachingParanamer;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.configuration.groovy.GroovyContext;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.groovy.GroovyStepsFactory;

import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;

public class TraderGroovyStories extends JUnitStories {

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration()
                .useStoryReporterBuilder(new StoryReporterBuilder().withFormats(HTML))
                .useParanamer();
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
