package org.jbehave.core.embedder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jbehave.core.model.Story;
import org.junit.Test;

public class StoryTimeoutsBehaviour {

	private EmbedderMonitor embedderMonitor = new NullEmbedderMonitor(); 
	private EmbedderControls embedderControls = new EmbedderControls();
	private Story story = mock(Story.class);

	@Test
	public void shouldSupportDefaultStoryTimeout(){
		when(story.getPath()).thenReturn("/any/path.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(300L));
	}

	@Test
	public void shouldSupportStoryTimeoutsByPathUsingAntPatterns(){
		embedderControls.useStoryTimeouts("**/*short*:50,**/*long*:500");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(500L));
	}

	@Test
	public void shouldSupportStoryTimeoutsByPathUsingRegexPatterns(){
		embedderControls.useStoryTimeouts("/[a-z]+/.*short.*:50,/[a-z]+/.*long.*:500");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(500L));
	}

	@Test
	public void shouldSupportStoryTimeoutsByPathUsingMixedPatterns(){
		embedderControls.useStoryTimeouts("/[a-z]+/.*short.*:50,**/*long*:500");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(500L));
	}

	private StoryTimeouts timeouts() {
		return new StoryTimeouts(embedderControls, embedderMonitor);
	}

}
