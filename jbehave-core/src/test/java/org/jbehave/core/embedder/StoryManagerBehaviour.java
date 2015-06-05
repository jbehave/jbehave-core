package org.jbehave.core.embedder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.codehaus.plexus.util.FileUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.embedder.StoryManager.EnqueuedStory;
import org.jbehave.core.embedder.StoryManager.RunningStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.junit.Test;

public class StoryManagerBehaviour {

	private PerformableTree performableTree = new PerformableTree();
	private RunContext context = mock(RunContext.class);
	private EmbedderMonitor embedderMonitor = new NullEmbedderMonitor(); 
	private EmbedderControls embedderControls = new EmbedderControls();
	private Story story = mock(Story.class);
	private ExecutorService executorService = mock(ExecutorService.class);
	private InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);

	@Test
	public void shouldEnsureStoryReportOutputDirectoryExistsWhenWritingStoryDurations() throws IOException{
		Configuration configuration = new MostUsefulConfiguration();
		configuration.storyReporterBuilder().withRelativeDirectory("inexistent");
		File outputDirectory = configuration.storyReporterBuilder().outputDirectory();
		FileUtils.deleteDirectory(outputDirectory); 
		assertThat(outputDirectory.exists(), is(false));
		StoryManager manager = new StoryManager(configuration, stepsFactory, embedderControls, embedderMonitor, executorService, performableTree);
		Collection<RunningStory> runningStories = new ArrayList<RunningStory>();
		manager.writeStoryDurations(runningStories);
		assertThat(outputDirectory.exists(), is(true));
	}

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
