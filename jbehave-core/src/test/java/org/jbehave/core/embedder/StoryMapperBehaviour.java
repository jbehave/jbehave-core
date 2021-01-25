package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jbehave.core.model.Description;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryMap;
import org.jbehave.core.model.StoryMaps;
import org.junit.jupiter.api.Test;

public class StoryMapperBehaviour {

    @Test
    public void shouldMapStoriesAllowedByFilter() {
        // Given
        Meta meta1 = mock(Meta.class, "meta1");
        Story story1 = new Story("/path/to/story1", Description.EMPTY, meta1, Narrative.EMPTY, asList(new Scenario("scenario1", meta1)));
        Meta meta2 = mock(Meta.class, "meta2");
        Story story2 = new Story("/path/to/story2", Description.EMPTY, meta2, Narrative.EMPTY, asList(new Scenario("scenario2", meta2)));
        MetaFilter filter = mock(MetaFilter.class);
        String filterAsString = "-some property";
        
        // When
        StoryMapper mapper = new StoryMapper();
        when(meta1.inheritFrom(meta1)).thenReturn(meta1);
        when(meta2.inheritFrom(meta2)).thenReturn(meta2);
        when(filter.allow(meta1)).thenReturn(false);
        when(filter.allow(meta2)).thenReturn(true);
        when(filter.asString()).thenReturn(filterAsString);
        mapper.map(story1, filter);
        mapper.map(story2, filter);

        // Then
        StoryMaps storyMaps = mapper.getStoryMaps();
        assertThat(storyMaps.getMaps().size(), equalTo(1));
        StoryMap storyMap = storyMaps.getMap(filterAsString);
        assertThat(storyMap.getMetaFilter(), equalTo(filterAsString));
        assertThat(storyMap.getStories().get(0).getPath(), equalTo(story2.getPath()));
        assertThat(storyMap.getStoryPaths(), equalTo(asList(story2.getPath())));
    }
  
}
