package org.jbehave.core.embedder;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;

/**
 * Provides the context for running a {@link Story}.
 */
public class StoryRunContext {

    private final List<CandidateSteps> steps;
    private final MetaFilter filter;
    private final Configuration configuration;
    private boolean givenStory;

    public StoryRunContext(Configuration configuration, MetaFilter filter, List<CandidateSteps> steps) {
        this(configuration, filter, steps, false);
    }

    private StoryRunContext(Configuration configuration, MetaFilter filter, List<CandidateSteps> steps,
            boolean givenStory) {
        this.configuration = configuration;
        this.filter = filter;
        this.steps = steps;
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

    public StoryRunContext forGivenStory() {
        return new StoryRunContext(configuration, filter, steps, true);
    }
}
