package org.jbehave.core;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.junit.Test;

public class RunnableStoryBehaviour {

    @SuppressWarnings("unchecked")
	@Test
    public void shouldRunASingleStoryAsClass() throws Throwable {
        // Given
        StoryEmbedder embedder = mock(StoryEmbedder.class);
        StoryConfiguration configuration = mock(StoryConfiguration.class);
        CandidateSteps steps = mock(CandidateSteps.class);
        Class<MyStory> storyClass = MyStory.class;

        // When
        RunnableStory story = new MyStory();
        story.useEmbedder(embedder);
        story.useConfiguration(configuration);
        story.addSteps(steps);
        story.run();

        // Then
        verify(embedder).runStoriesAsClasses(asList(storyClass));
        assertThat(story.getConfiguration(), sameInstance(configuration));
        assertThat(story.getSteps().get(0), sameInstance(steps));
    }


    @Test
    public void shouldRunMultipleStoriesAsPaths() throws Throwable {
        // Given
        StoryEmbedder embedder = mock(StoryEmbedder.class);
        StoryConfiguration configuration = mock(StoryConfiguration.class);
        CandidateSteps steps = mock(CandidateSteps.class);

        // When
        MyStories story = new MyStories();
        story.useEmbedder(embedder);
        story.useConfiguration(configuration);
        story.addSteps(steps);
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
        RunnableStory story = new MyStory();
        story.useEmbedder(embedder);
        story.addSteps(steps);
        assertThat(story.getConfiguration(), is(not(sameInstance(configuration))));
        story.useConfiguration(configuration);
        story.run();

        // Then
        assertThat(story.getConfiguration(), is(sameInstance(configuration)));
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
        RunnableStory story = new MyStory();
        story.useEmbedder(embedder);
        story.useConfiguration(configuration);
        story.addSteps(steps);
        story.run();

        // Then
        verify(embedder).runStoriesAsClasses(asList(storyClass));
    }

    private class MyStory extends JUnitStory {
        
    }

    private class MyStories extends JUnitStories {
        
        @Override
        protected List<String> storyPaths() {
            return asList("org/jbehave/core/story1", "org/jbehave/core/story2");
        }

    }


}
