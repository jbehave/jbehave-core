package org.jbehave.core.embedder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.embedder.StoryManager.EnqueuedStory;
import org.jbehave.core.model.Story;
import org.junit.Test;

public class StoryManagerBehaviour {

	private PerformableTree performableTree = new PerformableTree();
	private RunContext context = mock(RunContext.class);
	private EmbedderMonitor embedderMonitor = new NullEmbedderMonitor(); 
	private EmbedderControls embedderControls = new EmbedderControls();
	private Story story = mock(Story.class);

	@Test
	public void shouldSupportDefaultStoryTimeout(){
		assertThat(enqueuedStory(embedderMonitor, story).getTimeoutInSecs(), is(300L));
	}

	@Test
	public void shouldSupportStoryTimeoutsByPathUsingAntPatterns(){
		embedderControls.useStoryTimeoutInSecsByPath("**/*short*:50,**/*long*:500");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(enqueuedStory(embedderMonitor, story).getTimeoutInSecs(), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(enqueuedStory(embedderMonitor, story).getTimeoutInSecs(), is(500L));
	}

	@Test
	public void shouldSupportStoryTimeoutsByPathUsingRegexPatterns(){
		embedderControls.useStoryTimeoutInSecsByPath("/[a-z]+/.*short.*:50,/[a-z]+/.*long.*:500");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(enqueuedStory(embedderMonitor, story).getTimeoutInSecs(), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(enqueuedStory(embedderMonitor, story).getTimeoutInSecs(), is(500L));
	}

	@Test
	public void shouldSupportStoryTimeoutsByPathUsingMixedPatterns(){
		embedderControls.useStoryTimeoutInSecsByPath("/[a-z]+/.*short.*:50,**/*long*:500");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(enqueuedStory(embedderMonitor, story).getTimeoutInSecs(), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(enqueuedStory(embedderMonitor, story).getTimeoutInSecs(), is(500L));
	}

	private EnqueuedStory enqueuedStory(EmbedderMonitor embedderMonitor, Story story) {
		return new EnqueuedStory(performableTree, context, embedderControls, embedderMonitor, story);
	}

}
