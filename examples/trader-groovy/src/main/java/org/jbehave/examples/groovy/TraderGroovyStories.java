package org.jbehave.examples.groovy;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.groovy.GroovyStepsFactory;

public class TraderGroovyStories extends JUnitStories {

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration().useStoryReporterBuilder(new StoryReporterBuilder().withFormats(HTML));
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder()
                .findPaths(codeLocationFromClass(this.getClass()).getFile(), asList("**/*.story"), null);
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        List<String> groovyResources = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(),
                asList("**/*.groovy"), null);
        return new GroovyStepsFactory(new MostUsefulConfiguration(), groovyResources).createCandidateSteps();
    }

}
