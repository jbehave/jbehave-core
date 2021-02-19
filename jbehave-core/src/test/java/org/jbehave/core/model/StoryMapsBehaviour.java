package org.jbehave.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jbehave.core.io.UnderscoredToCapitalized;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class StoryMapsBehaviour {
    @Test
    void shouldMapStoriesByFilterInLanes() {
        // Given
        String storyPath1 = "/path/to/story_one.story"; 
        String storyPath2 = "/path/to/story_two.story";
        String storyPath3 = "/path/to/story_three.story";
        // story paths in non-natural order to verify ordering
        List<String> storyPaths = asList(storyPath2, storyPath1);
        Map<String, Story> storiesByPath = new HashMap<>();
        for (String storyPath : storyPaths) {
            storiesByPath.put(storyPath, new Story(storyPath));
        }
        
        // When
        StoryMaps storyMaps = new StoryMaps(asList(new StoryMap("filter", new HashSet<>(storiesByPath.values()))));
        StoryLanes storyLanes = new StoryLanes(storyMaps, new UnderscoredToCapitalized());
        
        // Then
        assertThat(storyMaps.toString(), containsString("filter"));
        assertThat(storyMaps.getMaps().toString(), containsString("filter"));
        assertThat(storyMaps.getMetaFilters(), equalTo(asList("filter")));
        List<Story> storiesFromLanes = storyLanes.getStories();
        assertThat(storiesFromLanes.size(), equalTo(2));
        // here we verify ordering
        assertThat(storiesFromLanes.get(0).getPath(), equalTo(storyPath1));
        assertThat(storiesFromLanes.get(1).getPath(), equalTo(storyPath2));
        assertThat(storyLanes.getLanes().size(), equalTo(1));
        assertThat(storyLanes.getLanes().get(0), equalTo("filter"));
        assertThat(storyLanes.inLane("filter", storiesByPath.get(storyPath1)), is(true));
        assertThat(storyLanes.inLane("filter", storiesByPath.get(storyPath2)), is(true));
        assertThat(storyLanes.inLane("filter", new Story(storyPath3)), is(false));
        assertThat(storyLanes.inLane("none", new Story(storyPath3)), is(false));
    }
}
