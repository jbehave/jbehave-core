package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryMap;
import org.jbehave.core.model.StoryMaps;

/**
 * Maps {@link Story}s by a {@link MetaFilter}.
 * 
 * @author Mauro Talevi
 */
public class StoryMapper {

    private Map<String, Set<Story>> map = new HashMap<String, Set<Story>>();

    /**
     * Maps a story if it is allowed by the meta filter
     * 
     * @param story
     *            the Story
     * @param metaFilter
     *            the meta filter
     */
    public void map(Story story, MetaFilter metaFilter) {
        if (metaFilter.allow(story.getMeta())) {
            boolean allowed = false;
            for (Scenario scenario : story.getScenarios()) {
                // scenario also inherits meta from story
                Meta inherited = scenario.getMeta().inheritFrom(story.getMeta());
                if (metaFilter.allow(inherited)) {
                    allowed = true;
                    break;
                }
            }
            if (allowed) {
                add(metaFilter.asString(), story);
            }
        }
    }

    public StoryMap getStoryMap(String filter) {
        return new StoryMap(filter, storiesFor(filter));
    }

    public StoryMaps getStoryMaps() {
        List<StoryMap> maps = new ArrayList<StoryMap>();
        for (String filter : map.keySet()) {
            maps.add(getStoryMap(filter));
        }
        return new StoryMaps(maps);
    }

    private void add(String filter, Story story) {
        storiesFor(filter).add(story);
    }

    private Set<Story> storiesFor(String filter) {
        Set<Story> stories = map.get(filter);
        if (stories == null) {
            stories = new HashSet<Story>();
            map.put(filter, stories);
        }
        return stories;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
