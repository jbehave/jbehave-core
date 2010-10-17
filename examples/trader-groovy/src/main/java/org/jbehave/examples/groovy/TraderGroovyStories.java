package org.jbehave.examples.groovy;

import groovy.lang.GroovyClassLoader;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;

/**
 */
public class TraderGroovyStories extends JUnitStories {
    private final GroovyClassLoader gcl = new GroovyClassLoader();

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration()
                .useStoryReporterBuilder(new StoryReporterBuilder().withFormats(HTML));
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder()
                .findPaths(codeLocationFromClass(this.getClass()).getFile(), asList("**/*.story"), null);
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        return new InstanceStepsFactory(configuration(), candidates())
                .createCandidateSteps();
    }

    private List<Object> candidates() {
        final ArrayList<Object> candidates = new ArrayList<Object>();
        candidates.add(groovyScript("/org/jbehave/examples/groovy/steps/ExampleGroovySteps.groovy"));
        return candidates;
    }

    private Object groovyScript(String filePath) {
        try {
            final URI groovyURI = this.getClass().getResource(filePath).toURI();
            final Class groovyClazz = gcl.parseClass(new File(groovyURI));
            return groovyClazz.newInstance();

        } catch (Exception e) {
            throw new RuntimeException(format("Could not create instance of Groovy script '%s'.", filePath), e);
        }
    }
}
