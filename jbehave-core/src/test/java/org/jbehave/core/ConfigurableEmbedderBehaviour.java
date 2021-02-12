package org.jbehave.core;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

class ConfigurableEmbedderBehaviour {

    @Test
    void shouldRunASingleStory() {
        // Given
        Embedder embedder = mock(Embedder.class);
        Configuration configuration = mock(Configuration.class);
        StoryPathResolver pathResolver = mock(StoryPathResolver.class);
        when(embedder.configuration()).thenReturn(configuration);
        when(configuration.storyPathResolver()).thenReturn(pathResolver);
        Class<MyStory> storyClass = MyStory.class;
        String storyPath = "/path/to/story";
        when(pathResolver.resolve(storyClass)).thenReturn(storyPath);
        InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);

        // When
        MyStory story = new MyStory(configuration, stepsFactory);
        story.useEmbedder(embedder);
        story.run();

        // Then
        verify(embedder, times(2)).useConfiguration(configuration);
        verify(embedder, times(2)).useStepsFactory(stepsFactory);
        verify(embedder).runStoriesAsPaths(asList(storyPath));
    }


    @Test
    void shouldRunMultipleStories() {
        // Given
        Embedder embedder = mock(Embedder.class);
        Configuration configuration = mock(Configuration.class);
        InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);

        // When
        MyStories stories = new MyStories(configuration, stepsFactory);
        stories.useEmbedder(embedder);
        stories.run();

        // Then
        verify(embedder).useConfiguration(configuration);
        verify(embedder).useStepsFactory(stepsFactory);
        verify(embedder).runStoriesAsPaths(asList("org/jbehave/core/story1", "org/jbehave/core/story2"));
    }

    @Test
    void shouldAllowOverrideOfDefaultConfiguration() {
        // Given
        Embedder embedder = mock(Embedder.class);
        Configuration configuration = mock(Configuration.class);
        StoryPathResolver pathResolver = mock(StoryPathResolver.class);
        when(embedder.configuration()).thenReturn(configuration);
        when(configuration.storyPathResolver()).thenReturn(pathResolver);
        Class<MyStory> storyClass = MyStory.class;
        String storyPath = "/path/to/story";
        when(pathResolver.resolve(storyClass)).thenReturn(storyPath);
        InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);
        
        // When
        MyStory story = new MyStory(new MostUsefulConfiguration(), stepsFactory);
        assertThat(story.configuration(), is(not(sameInstance(configuration))));
        story.useConfiguration(configuration);
        assertThat(story.configuration(), is(sameInstance(configuration)));
        story.useEmbedder(embedder);
        story.run();

        // Then
        verify(embedder, times(2)).useConfiguration(configuration);
        verify(embedder, times(2)).useStepsFactory(stepsFactory);
        verify(embedder).runStoriesAsPaths(asList(storyPath));
    }

    @Test
    void shouldAllowAdditionOfSteps() {
        // Given
        Embedder embedder = mock(Embedder.class);
        Configuration configuration = mock(Configuration.class);
        StoryPathResolver pathResolver = mock(StoryPathResolver.class);
        when(embedder.configuration()).thenReturn(configuration);
        when(configuration.storyPathResolver()).thenReturn(pathResolver);
        Class<MyStory> storyClass = MyStory.class;
        String storyPath = "/path/to/story";
        when(pathResolver.resolve(storyClass)).thenReturn(storyPath);
        InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);
        
        // When
        MyStory story = new MyStory(configuration, stepsFactory);
        story.useEmbedder(embedder);
        story.run();

        // Then
        verify(embedder).runStoriesAsPaths(asList(storyPath));
    }

    private class MyStory extends JUnitStory {

        public MyStory(Configuration configuration, InjectableStepsFactory stepsFactory) {
            useConfiguration(configuration);
            useStepsFactory(stepsFactory);
        }
        
    }

    private class MyStories extends JUnitStories {
        
        public MyStories(Configuration configuration, InjectableStepsFactory stepsFactory) {
            useConfiguration(configuration);
            useStepsFactory(stepsFactory);
        }

        @Override
        public List<String> storyPaths() {
            return asList("org/jbehave/core/story1", "org/jbehave/core/story2");
        }

    }


}
