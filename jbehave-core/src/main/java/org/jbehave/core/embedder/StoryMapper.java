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

/**
 * Maps {@link Story}s by {@link MetaFilter}.
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
     * @param filter
     *            the MetaFilter
     */
    public void map(Story story, MetaFilter filter) throws Throwable {
        if (filter.allow(story.getMeta())) {
            List<Scenario> allowed = new ArrayList<Scenario>();
            for (Scenario scenario : story.getScenarios()) {
                // scenario also inherits meta from story
                if (filter.allow(Meta.inherit(scenario.getMeta(), story.getMeta()))) {
                    allowed.add(scenario);
                }
            }

            add(filter.asString(), filteredStory(story, allowed));
        }
    }

    private Story filteredStory(Story story, List<Scenario> scenarios) {
        return new Story(story.getPath(), story.getDescription(), story.getMeta(), story.getNarrative(), scenarios);
    }

    public StoryMap getStoryMap(String filter) {
        return new StoryMap(filter, storiesFor(filter));
    }

    public void clear(String filter) {
        map.remove(filter);
    }

    public void clearAll() {
        map.clear();
    }

    private void add(String filter, Story story) {
        Set<Story> stories = storiesFor(filter);
        stories.add(story);
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
