package org.jbehave.core;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.jbehave.core.configuration.PropertyBasedStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.junit.Test;

public class RunnableStoryBehaviour {

    @SuppressWarnings("unchecked")
	@Test
    public void shouldRunASingleStoryViaClass() throws Throwable {
        // Given
        StoryEmbedder embedder = mock(StoryEmbedder.class);
        StoryConfiguration configuration = mock(StoryConfiguration.class);
        CandidateSteps steps = mock(CandidateSteps.class);
        Class<MyStory> storyClass = MyStory.class;

        // When
        RunnableStory story = new MyStory(embedder, configuration, steps);
        story.run();

        verify(embedder).runStoriesAsClasses(asList(storyClass));
    }


    @Test
    public void shouldRunMultipleStoriesViaPaths() throws Throwable {
        // Given
        StoryEmbedder embedder = mock(StoryEmbedder.class);
        StoryConfiguration configuration = mock(StoryConfiguration.class);
        CandidateSteps steps = mock(CandidateSteps.class);

        // When
        MyStories story = new MyStories(embedder, configuration, steps);
        story.run();

        // Then
        verify(embedder).runStoriesAsPaths(asList("org/jbehave/core/story1", "org/jbehave/core/story2"));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void shouldAllowOverrideOfDefaultConfiguration() throws Throwable {
        // Given
        StoryEmbedder embedder = mock(StoryEmbedder.class);
        StoryConfiguration configuration = mock(StoryConfiguration.class);
        CandidateSteps steps = mock(CandidateSteps.class);
        Class<MyStory> storyClass = MyStory.class;

        // When
        RunnableStory story = new MyStory(embedder, steps);
        assertThat(story.getConfiguration(), is(not(sameInstance(configuration))));
        story.useConfiguration(configuration);
        story.run();

        // Then
        assertThat(story.getConfiguration(), is(not(instanceOf(PropertyBasedStoryConfiguration.class))));
        verify(embedder).runStoriesAsClasses(asList(storyClass));
    }


    @SuppressWarnings("unchecked")
	@Test
    public void shouldAllowAdditionOfSteps() throws Throwable {
        // Given
        StoryEmbedder embedder = mock(StoryEmbedder.class);
        StoryConfiguration configuration = mock(StoryConfiguration.class);
        CandidateSteps steps = mock(CandidateSteps.class);
        Class<MyStory> storyClass = MyStory.class;

        // When
        RunnableStory story = new MyStory(embedder, configuration);
        story.addSteps(steps);
        story.run();

        // Then
        verify(embedder).runStoriesAsClasses(asList(storyClass));
    }

    private class MyStory extends JUnitStory {
        private StoryEmbedder embedder;

        public MyStory(StoryEmbedder embedder, CandidateSteps... steps) {
            this.embedder = embedder;
            addSteps(steps);
        }

        public MyStory(StoryEmbedder embedder, StoryConfiguration configuration, CandidateSteps... steps) {
            this.embedder = embedder;
            useConfiguration(configuration);
            addSteps(steps);
        }

        @Override
        protected StoryEmbedder configuredEmbedder() {
            return embedder;
        }
    }

    private class MyStories extends JUnitStories {
        private StoryEmbedder embedder;

        public MyStories(StoryEmbedder embedder, StoryConfiguration configuration, CandidateSteps... steps) {
            this.embedder = embedder;
            useConfiguration(configuration);
            addSteps(steps);
        }

        @Override
        protected List<String> storyPaths() {
            return asList("org/jbehave/core/story1", "org/jbehave/core/story2");
        }

        protected StoryEmbedder configuredEmbedder() {
            return embedder;
        }
    }


}
