package org.jbehave.examples.trader.guice;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.guice.GuiceStepsFactory;
import org.jbehave.examples.trader.TraderStories;
import org.jbehave.examples.trader.guice.AnnotatedEmbedderUsingGuice.StepsModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Run trader stories using GuiceStepsFactory. The textual trader stories are
 * exactly the same ones found in the jbehave-trader-example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class TraderStoriesUsingGuice extends TraderStories {

    @Override
    public List<CandidateSteps> candidateSteps() {
        return new GuiceStepsFactory(configuration(), createInjector()).createCandidateSteps();
    }

    private Injector createInjector() {
        return Guice.createInjector(new StepsModule());
    }

    @Override
    protected List<String> storyPaths() {
        String searchInDirectory = codeLocationFromPath("../trader/src/main/java").getFile();
        return new StoryFinder().findPaths(searchInDirectory, asList("**/*.story"), null);
    }

}
