package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jbehave.core.io.StoryNameResolver;

/**
 * Represents a <a href="http://en.wikipedia.org/wiki/Swim_lane">Swim Lane</a>
 * view of {@link StoryMaps}.
 */
public class StoryLanes {

    private final StoryMaps storyMaps;
    private final StoryNameResolver nameResolver;

    public StoryLanes(StoryMaps storyMaps, StoryNameResolver nameResolver) {
        this.storyMaps = storyMaps;
        this.nameResolver = nameResolver;
    }

    public List<Story> getStories() {
        List<Story> stories = new ArrayList<>(laneStories("")); // returns all stories
        Collections.sort(stories, new Comparator<Story>() {
            @Override
            public int compare(Story o1, Story o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return stories;
    }

    public List<String> getLanes() {
        List<String> lanes = storyMaps.getMetaFilters();
        lanes.remove(""); // don't want to display all stories again
        Collections.sort(lanes);
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
        StoryMap storyMap = storyMaps.getMap(lane);
        List<Story> stories = new ArrayList<>();
        if (storyMap == null) {
            for (StoryMap map : storyMaps.getMaps()) {
                stories.addAll(map.getStories());
            }
        } else {
            stories.addAll(storyMap.getStories());            
        }
        nameStories(stories);
        return stories;
    }

    private void nameStories(List<Story> stories) {
        for (Story story : stories) {
            story.namedAs(nameResolver.resolveName(story.getPath()));
        }
        
    }

}
