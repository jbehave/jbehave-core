package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

public class FilteredStory {

    private final boolean filterIgnored;
    private final boolean storyExcluded;
    private final List<Scenario> scenariosIncluded = new ArrayList<>();

    public FilteredStory(MetaFilter filter, Story story, StoryControls storyControls) {
        this(filter, story, storyControls, false);
    }
    
    public FilteredStory(MetaFilter filter, Story story, StoryControls storyControls, boolean givenStory) {
        filterIgnored = givenStory && storyControls.ignoreMetaFiltersIfGivenStory();
        String storyMetaPrefix = storyControls.storyMetaPrefix();
        String scenarioMetaPrefix = storyControls.scenarioMetaPrefix();
        Meta storyMeta = story.getMeta().inheritFrom(story.asMeta(storyMetaPrefix));
        storyExcluded = filter.excluded(storyMeta);
        for (Scenario scenario : story.getScenarios()) {
            if (scenario.getExamplesTable().getRowCount() > 0 && metaByRow(scenario, storyControls)) {
                // allow filtering on meta by row
                scenariosIncluded.add(scenario);
            } else {
                Meta scenarioMeta = scenario.getMeta().inheritFrom(
                        scenario.asMeta(scenarioMetaPrefix).inheritFrom(storyMeta));
                if (!filter.excluded(scenarioMeta)) {
                    scenariosIncluded.add(scenario);
                }
            }
        }
    }

    public boolean excluded() {
        return !filterIgnored && storyExcluded && scenariosIncluded.isEmpty();
    }

    public boolean excluded(Scenario scenario) {
        return !filterIgnored && !scenariosIncluded.contains(scenario);
    }

    private boolean metaByRow(Scenario scenario, StoryControls storyControls) {
        if (scenario.getExamplesTable().getProperties().containsKey("metaByRow")) {
            return scenario.getExamplesTable().metaByRow();
        }

        return storyControls.metaByRow();
    }
}
