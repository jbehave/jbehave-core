package org.jbehave.core.reporters;

import org.jbehave.core.context.Context;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

public class ContextStoryReporter extends NullStoryReporter {
    private final Context context;

    public ContextStoryReporter(Context context) {
        this.context = context;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        context.setCurrentStory(story.getPath());
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        context.setCurrentScenario(scenario.getTitle());
    }
}
