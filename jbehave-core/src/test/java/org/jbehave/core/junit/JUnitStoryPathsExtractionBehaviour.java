package org.jbehave.core.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryPathResolver;
import org.junit.jupiter.api.Test;

class JUnitStoryPathsExtractionBehaviour {

    private static final List<String> JUNIT_STORIES_PATHS = Arrays.asList("/path/story1.story", "/path/story2.story");

    @Test
    void shouldExtractStoryPathsFromJUnitStory() {
        TestJUnitStory testJUnitStory = new TestJUnitStory();
        Embedder embedder = mock(Embedder.class);
        testJUnitStory.useEmbedder(embedder);
        Configuration configuration = mock(Configuration.class);
        when(embedder.configuration()).thenReturn(configuration);
        StoryPathResolver storyPathResolver = mock(StoryPathResolver.class);
        when(configuration.storyPathResolver()).thenReturn(storyPathResolver);
        String storyPath = "/path/story.story";
        when(storyPathResolver.resolve(TestJUnitStory.class)).thenReturn(storyPath);
        assertEquals(Collections.singletonList(storyPath), testJUnitStory.storyPaths());
    }

    @Test
    void shouldExtractStoryPathsFromJUnitStories() {
        assertEquals(JUNIT_STORIES_PATHS, new TestJUnitStories().storyPaths());
    }

    @Test
    void shouldExtractStoryPathsFromJUnitStoriesChild(){
        assertEquals(JUNIT_STORIES_PATHS, new ChildTestJUnitStories().storyPaths());
    }

    public static class TestJUnitStory extends JUnitStory {
    }

    public static class TestJUnitStories extends JUnitStories {
        @Override
        public List<String> storyPaths() {
            return JUNIT_STORIES_PATHS;
        }
    }

    public static class ChildTestJUnitStories extends TestJUnitStories {
    }

    public static class TestConfigurableEmbedder extends ConfigurableEmbedder {
        @Override
        @org.junit.Test
        public void run() {
            fail("Should not run");
        }
    }
}
