package org.jbehave.core.embedder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jbehave.core.embedder.StoryTimeouts.TimeoutParser;
import org.jbehave.core.model.Story;
import org.junit.Test;
import org.mockito.Mockito;

public class StoryTimeoutsBehaviour {

	private EmbedderMonitor embedderMonitor = new NullEmbedderMonitor();
	private EmbedderControls embedderControls = new EmbedderControls();
	private Story story = mock(Story.class);

	@Test
	public void shouldAllowADefaultTimeout() {
		when(story.getPath()).thenReturn("/any/path.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(300L));
	}

	@Test
	public void shouldAllowTimeoutByPathUsingAntPatterns() {
		embedderControls.useStoryTimeouts("**/*short*:50,**/*long*:500");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(500L));
	}

	@Test
	public void shouldAllowTimeoutByPathUsingRegexPatterns() {
		embedderControls
				.useStoryTimeouts("/[a-z]+/.*short.*:50,/[a-z]+/.*long.*:500");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(500L));
	}

	@Test
	public void shouldAllowTimeoutByPathUsingMixedPatterns() {
		embedderControls.useStoryTimeouts("/[a-z]+/.*short.*:50,**/*long*:500");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(500L));
	}

	@Test
	public void shouldAllowTimeoutToBeSpecifiedyBySimpleTextualFormat() {
		embedderControls.useStoryTimeouts("50");
		assertThat(timeouts().getTimeoutInSecs(story), is(50L));
		embedderControls.useStoryTimeouts("50s");
		assertThat(timeouts().getTimeoutInSecs(story), is(50L));
		embedderControls.useStoryTimeouts("5m 30s");
		assertThat(timeouts().getTimeoutInSecs(story), is(330L));
		embedderControls.useStoryTimeouts("1h 30m 15s");
		assertThat(timeouts().getTimeoutInSecs(story), is(5415L));
		embedderControls.useStoryTimeouts("1d 12h 30m 15s");
		assertThat(timeouts().getTimeoutInSecs(story), is(131415L));
	}

	@Test
	public void shouldAllowTimeoutByPathUsingMixedFormats() {
		embedderControls.useStoryTimeouts("**/.*short.*:50,**/*long*:5m");
		when(story.getPath()).thenReturn("/path/to/a_short_and_sweet.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(50L));
		when(story.getPath()).thenReturn("/path/to/a_long_and_winding.story");
		assertThat(timeouts().getTimeoutInSecs(story), is(300L));
	}
	
	@Test
	public void shouldAllowCustomTimeoutParser() {
		TimeoutParser timeoutParser = mock(TimeoutParser.class);
		when(timeoutParser.isValid(Mockito.anyString())).thenReturn(true);
		when(timeoutParser.asSeconds(Mockito.anyString())).thenReturn(1L);
		assertThat(timeouts().withParsers(timeoutParser).getTimeoutInSecs(story), is(1L));
	}

	private StoryTimeouts timeouts() {
		return new StoryTimeouts(embedderControls, embedderMonitor);
	}

}
