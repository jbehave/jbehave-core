package org.jbehave.core.embedder;

import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

public class FilteredStory {

    private boolean storyAllowed;
    private Map<Scenario, Boolean> scenariosAllowed;

    public FilteredStory(MetaFilter filter, Story story, StoryControls storyControls) {
        String storyMetaPrefix = storyControls.storyMetaPrefix();
        String scenarioMetaPrefix = storyControls.scenarioMetaPrefix();
        Meta storyMeta = story.getMeta().inheritFrom(story.asMeta(storyMetaPrefix));
        storyAllowed = filter.allow(storyMeta);
        scenariosAllowed = new HashMap<Scenario, Boolean>();
        for (Scenario scenario : story.getScenarios()) {
            Meta scenarioMeta = scenario.getMeta().inheritFrom(
                    scenario.asMeta(scenarioMetaPrefix).inheritFrom(storyMeta));
            boolean scenarioAllowed = filter.allow(scenarioMeta);
            scenariosAllowed.put(scenario, scenarioAllowed);
        }
    }

    public boolean allowed() {
        return storyAllowed || scenariosAllowed.values().contains(true);
    }

    public boolean allowed(Scenario scenario) {
        return scenariosAllowed.get(scenario);
    }
}