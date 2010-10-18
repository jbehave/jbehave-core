package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.io.StoryNameResolver;

/**
 *  Represents a <a href="http://en.wikipedia.org/wiki/Swim_lane">Swim Lane</a> view of 
 *  {@link StoryMap}s.
 */
public class StoryLanes {

    private Map<String, StoryMap> indexed = new HashMap<String, StoryMap>();
    private final StoryNameResolver nameResolver;

    public StoryLanes(List<StoryMap> storyMaps, StoryNameResolver nameResolver) {
        this.nameResolver = nameResolver;
        index(storyMaps);
    }

    private void index(List<StoryMap> storyMaps) {
        for (StoryMap storyMap : storyMaps) {
            indexed.put(storyMap.getMetaFilter(), storyMap);
            for (Story story : storyMap.getStories()) {
                story.namedAs(nameResolver.resolveName(story.getPath()));
            }
        }
    }

    public List<Story> getStories() {
        return laneStories(""); // returns all stories
    }

    public List<String> getLanes() {
        List<String> lanes = new ArrayList<String>(indexed.keySet());
        lanes.remove(""); // don't want to display all stories again
        return lanes;
    }

    public boolean inLane(String lane, Story story) {
        for (Story laneStory : laneStories(lane)) {
            if (laneStory.getPath().equals(story.getPath())) {
                return true;
            }
        }
        return false;
    }

    private List<Story> laneStories(String lane) {
        StoryMap storyMap = indexed.get(lane);
        if (storyMap == null) {
            return new ArrayList<Story>();
        }
        return storyMap.getStories();
    }

}