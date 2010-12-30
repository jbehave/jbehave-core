package org.jbehave.core.embedder;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;

/**
 * Provides the context for running a {@link Story}.
 */
public class StoryRunContext {

    private final List<CandidateSteps> steps;
    private final MetaFilter filter;
    private final Configuration configuration;
    private final String path;
    private final boolean givenStory;

    public StoryRunContext(Configuration configuration, MetaFilter filter, List<CandidateSteps> steps, String path) {
        this(configuration, filter, steps, path, false);
    }

    private StoryRunContext(Configuration configuration, MetaFilter filter, List<CandidateSteps> steps, String path,
            boolean givenStory) {
        this.configuration = configuration;
        this.filter = filter;
        this.steps = steps;
        this.path = path;
        this.givenStory = givenStory;
    }

    public boolean dryRun() {
        return configuration.storyControls().dryRun();
    }

    public Configuration configuration() {
        return configuration;
    }

    public boolean givenStory() {
        return givenStory;
    }

    public List<CandidateSteps> candidateSteps() {
        return steps;
    }

    public MetaFilter metaFilter() {
        return filter;
    }

    public String path() {
        return path;
    }

    public StoryRunContext forGivenStory(GivenStory story) {
        String actualPath = configuration.pathCalculator().calculate(path, story.getPath());
        return new StoryRunContext(configuration, filter, steps, actualPath, true);
    }
}
