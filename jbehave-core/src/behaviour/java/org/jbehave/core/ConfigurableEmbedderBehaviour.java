package org.jbehave.core;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.CandidateSteps;
import org.junit.Test;
import org.mockito.Mockito;

public class ConfigurableEmbedderBehaviour {

    @SuppressWarnings("unchecked")
	@Test
    public void shouldRunASingleStoryAsClass() throws Throwable {
        // Given
        Embedder embedder = mock(Embedder.class);
        Configuration configuration = mock(Configuration.class);
        CandidateSteps steps = mock(CandidateSteps.class);
        Class<MyStory> storyClass = MyStory.class;

        // When
        MyStory story = new MyStory(configuration, steps);
        story.useEmbedder(embedder);
        story.run();

        // Then
        verify(embedder).useConfiguration(configuration);
        verify(embedder).useCandidateSteps(eq(asList(steps)));
        verify(embedder).runStoriesAsClasses(asList(storyClass));
    }


    @Test
    public void shouldRunMultipleStoriesAsPaths() throws Throwable {
        // Given
        Embedder embedder = mock(Embedder.class);
        Configuration configuration = mock(Configuration.class);
        CandidateSteps steps = mock(CandidateSteps.class);

        // When
        MyStories story = new MyStories(configuration, steps);
        story.useEmbedder(embedder);
        story.run();

        // Then
        verify(embedder).runStoriesAsPaths(asList("org/jbehave/core/story1", "org/jbehave/core/story2"));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void shouldAllowOverrideOfDefaultConfiguration() throws Throwable {
        // Given
        Embedder embedder = mock(Embedder.class);
        Configuration configuration = mock(Configuration.class);
        CandidateSteps steps = mock(CandidateSteps.class);
        Class<MyStory> storyClass = MyStory.class;

        // When
        MyStory story = new MyStory(new MostUsefulConfiguration(), steps);
        assertThat(story.configuration(), is(not(sameInstance(configuration))));
        story.useConfiguration(configuration);
        assertThat(story.configuration(), is(sameInstance(configuration)));
        story.useEmbedder(embedder);
        story.run();

        // Then
        verify(embedder).useConfiguration(configuration);
        verify(embedder).useCandidateSteps(Mockito.eq(Arrays.asList(steps)));
        verify(embedder).runStoriesAsClasses(asList(storyClass));
    }


    @SuppressWarnings("unchecked")
	@Test
    public void shouldAllowAdditionOfSteps() throws Throwable {
        // Given
        Embedder embedder = mock(Embedder.class);
        Configuration configuration = mock(Configuration.class);
        CandidateSteps steps = mock(CandidateSteps.class);
        Class<MyStory> storyClass = MyStory.class;

        // When
        MyStory story = new MyStory(configuration, steps);
        story.useEmbedder(embedder);
        story.run();

        // Then
        verify(embedder).runStoriesAsClasses(asList(storyClass));
    }

    private class MyStory extends JUnitStory {

        public MyStory(Configuration configuration, CandidateSteps steps) {
            useConfiguration(configuration);
            addSteps(steps);
        }
        
    }

    private class MyStories extends JUnitStories {
        
        public MyStories(Configuration configuration, CandidateSteps steps) {
            useConfiguration(configuration);
            addSteps(steps);
        }

        @Override
        protected List<String> storyPaths() {
            return asList("org/jbehave/core/story1", "org/jbehave/core/story2");
        }

    }


}
