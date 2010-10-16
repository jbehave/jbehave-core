package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryMap;

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
            List<Scenario> allowed = new ArrayList<Scenario>();
            for (Scenario scenario : story.getScenarios()) {
                // scenario also inherits meta from story
                if (metaFilter.allow(Meta.inherit(scenario.getMeta(), story.getMeta()))) {
                    allowed.add(scenario);
                }
            }
            add(metaFilter.asString(), filteredStory(story, allowed));
        }
    }

    private Story filteredStory(Story story, List<Scenario> scenarios) {
        Story filtered = new Story(story.getPath(), story.getDescription(), story.getMeta(), story.getNarrative(), scenarios);
        String name = StringUtils.substringAfterLast(story.getPath(), "/");
        filtered.namedAs(name);        
        return filtered;
    }

    public StoryMap getStoryMap(String filter) {
        return new StoryMap(filter, storiesFor(filter));
    }

    public List<StoryMap> getStoryMaps() {
        List<StoryMap> maps = new ArrayList<StoryMap>();
        for (String filter : map.keySet()) {
            maps.add(getStoryMap(filter));
        }
        return maps;
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
