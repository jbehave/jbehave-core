package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.RunnableStory;
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderConfiguration;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.PrintStreamEmbedderMonitor;
import org.jbehave.core.embedder.StoryRunner;
import org.jbehave.core.embedder.Embedder.RenderingReportsFailedException;
import org.jbehave.core.embedder.Embedder.RunningStoriesFailedException;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.reporters.FreemarkerReportRenderer;
import org.jbehave.core.reporters.ReportRenderer;
import org.jbehave.core.steps.CandidateSteps;
import org.junit.Test;

public class EmbedderBehaviour {

	@Test
	public void shouldRunStoriesAsRunnables() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		StoryConfiguration configuration = new MostUsefulStoryConfiguration();
		CandidateSteps steps = mock(CandidateSteps.class);
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		embedder.useConfiguration(configuration);
		embedder.useCandidateSteps(asList(steps));
		for (RunnableStory story : runnables) {
			doNothing().when(story).run();
		}
		embedder.runStories(runnables);

		// Then
		for (RunnableStory story : runnables) {
			verify(story).useEmbedder(embedder);
			assertThat(out.toString(), containsString("Running story "
					+ story.getClass().getName()));
		}
		assertThat(out.toString(), containsString("Rendering reports"));
		assertThat(out.toString(), containsString("Reports rendered"));
	}

	@Test
	public void shouldNotRunStoriesAsRunnablesIfSkipFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doSkip(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		for (RunnableStory story : runnables) {
			doNothing().when(story).run();
		}
		embedder.runStories(runnables);

		// Then
		for (RunnableStory story : runnables) {
			verify(story, never()).useEmbedder(embedder);
			assertThat(out.toString(), not(containsString("Running story "
					+ story.getClass().getName())));
		}
		assertThat(out.toString(), not(containsString("Rendering reports")));
	}

	@Test(expected = RunningStoriesFailedException.class)
	public void shouldThrowExceptionUponFailingStoriesAsRunnablesIfIgnoreFailureInStoriesFlagIsNotSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		for (RunnableStory story : runnables) {
			doThrow(new RuntimeException(story + " failed")).when(story).run();
		}
		embedder.runStories(runnables);

	}

	@Test
	public void shouldNotThrowExceptionUponFailingStoriesAsRunnablesIfIgnoreFailureInStoriesFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doIgnoreFailureInStories(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		for (RunnableStory story : runnables) {
			doThrow(new RuntimeException(story + " failed")).when(story).run();
		}
		embedder.runStories(runnables);

		// Then
		for (RunnableStory story : runnables) {
			String storyName = story.getClass().getName();
			assertThat(out.toString(), containsString("Running story "
					+ storyName));
			assertThat(out.toString(), containsString("Failed to run story "
					+ storyName));
		}

	}

	@Test
	public void shouldRunStoriesAsRunnablesInBatchIfBatchFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doBatch(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		for (RunnableStory story : runnables) {
			doNothing().when(story).run();
		}
		embedder.runStories(runnables);

		// Then
		for (RunnableStory story : runnables) {
			String storyName = story.getClass().getName();
			assertThat(out.toString(), containsString("Running story "
					+ storyName));
		}
	}


	@Test(expected = RunningStoriesFailedException.class)
	public void shouldThrowExceptionUponFailingStoriesAsRunnablesInBatchIfIgnoreFailureInStoriesFlagIsNotSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doBatch(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		for (RunnableStory story : runnables) {
			doThrow(new RuntimeException(story + " failed")).when(story).run();
		}
		embedder.runStories(runnables);

		// Then fail as expected

	}

	@Test
	public void shouldRunFailingStoriesAsRunnablesInBatchIfBatchFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doBatch(true).doIgnoreFailureInStories(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		for (RunnableStory story : runnables) {
			doThrow(new RuntimeException(story + " failed")).when(story).run();
		}
		embedder.runStories(runnables);

		// Then
		for (RunnableStory story : runnables) {
			String storyName = story.getClass().getName();
			assertThat(out.toString(), containsString("Running story "
					+ storyName));
		}
		assertThat(out.toString(),
				containsString("Failed to run batch stories"));
	}

	@Test
	public void shouldNotRenderReportsWhenRunningStoriesAsRunnablesIfRenderReportsAfterStoriesFlagIsNotSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doRenderReportsAfterStories(false);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		for (RunnableStory story : runnables) {
			doNothing().when(story).run();
		}
		embedder.runStories(runnables);

		// Then
		for (RunnableStory story : runnables) {
			String storyName = story.getClass().getName();
			assertThat(out.toString(), containsString("Running story "
					+ storyName));
		}
		assertThat(out.toString(), not(containsString("Rendering reports")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldRunStoriesAsClasses() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		embedder.runStoriesAsClasses(storyClasses);

		// Then
		StoryConfiguration configuration = embedder.storyConfiguration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		for (Class storyClass : storyClasses) {
			String storyPath = resolver.resolve(storyClass);
			verify(runner).run(configuration, candidateSteps, storyPath);
			assertThat(out.toString(), containsString("Running story "
					+ storyPath));
		}
		assertThat(out.toString(), containsString("Rendering reports"));
		assertThat(out.toString(), containsString("Reports rendered"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldRunStoriesAsPaths() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		StoryConfiguration configuration = embedder.storyConfiguration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class storyClass : storyClasses) {
			storyPaths.add(resolver.resolve(storyClass));
		}
		embedder.runStoriesAsPaths(storyPaths);

		// Then
		for (String storyPath : storyPaths) {
			verify(runner).run(configuration, candidateSteps, storyPath);
			assertThat(out.toString(), containsString("Running story "
					+ storyPath));
		}
		assertThat(out.toString(), containsString("Rendering reports"));
		assertThat(out.toString(), containsString("Reports rendered"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotRunStoriesIfSkipFlagIsSet() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doSkip(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		StoryConfiguration configuration = embedder.storyConfiguration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class storyClass : storyClasses) {
			storyPaths.add(resolver.resolve(storyClass));
		}
		embedder.runStoriesAsPaths(storyPaths);

		// Then
		for (String storyPath : storyPaths) {
			verify(runner, never()).run(configuration, candidateSteps,
					storyPath);
			assertThat(out.toString(), not(containsString("Running story "
					+ storyPath)));
		}
		assertThat(out.toString(), containsString("Stories not run"));
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RunningStoriesFailedException.class)
	public void shouldThrowExceptionUponFailingStoriesAsPathsIfIgnoreFailureInStoriesFlagIsNotSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		StoryConfiguration configuration = embedder.storyConfiguration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class storyClass : storyClasses) {
			storyPaths.add(resolver.resolve(storyClass));
		}
		for (String storyPath : storyPaths) {
			doThrow(new RuntimeException(storyPath + " failed")).when(runner)
					.run(configuration, candidateSteps, storyPath);
		}
		embedder.runStoriesAsPaths(storyPaths);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotThrowExceptionUponFailingStoriesAsPathsIfIgnoreFailureInStoriesFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doIgnoreFailureInStories(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		StoryConfiguration configuration = embedder.storyConfiguration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class storyClass : storyClasses) {
			storyPaths.add(resolver.resolve(storyClass));
		}
		for (String storyPath : storyPaths) {
			doThrow(new RuntimeException(storyPath + " failed")).when(runner)
					.run(configuration, candidateSteps, storyPath);
		}
		embedder.runStoriesAsPaths(storyPaths);

		// Then
		for (String storyPath : storyPaths) {
			assertThat(out.toString(), containsString("Running story "
					+ storyPath));
			assertThat(out.toString(), containsString("Failed to run story "
					+ storyPath));
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldRunStoriesAsPathsInBatchIfBatchFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doBatch(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		StoryConfiguration configuration = embedder.storyConfiguration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class storyClass : storyClasses) {
			storyPaths.add(resolver.resolve(storyClass));
		}
		for (String storyPath : storyPaths) {
			doNothing().when(runner).run(configuration, candidateSteps,
					storyPath);
		}
		embedder.runStoriesAsPaths(storyPaths);

		// Then
		for (String storyPath : storyPaths) {
			assertThat(out.toString(), containsString("Running story "
					+ storyPath));
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RunningStoriesFailedException.class)
	public void shouldThrowExceptionUponFailingStoriesAsPathsInBatchIfIgnoreFailureInStoriesFlagIsNotSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doBatch(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		StoryConfiguration configuration = embedder.storyConfiguration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class storyClass : storyClasses) {
			storyPaths.add(resolver.resolve(storyClass));
		}
		for (String storyPath : storyPaths) {
			doThrow(new RuntimeException(storyPath + " failed")).when(runner)
					.run(configuration, candidateSteps, storyPath);
		}
		embedder.runStoriesAsPaths(storyPaths);

		// Then fail as expected

	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldRunFailingStoriesAsPathsInBatchIfBatchFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doBatch(true).doIgnoreFailureInStories(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		StoryConfiguration configuration = embedder.storyConfiguration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class storyClass : storyClasses) {
			storyPaths.add(resolver.resolve(storyClass));
		}
		for (String storyPath : storyPaths) {
			doThrow(new RuntimeException(storyPath + " failed")).when(runner)
					.run(configuration, candidateSteps, storyPath);
		}
		embedder.runStoriesAsPaths(storyPaths);

		// Then
		for (String storyPath : storyPaths) {
			assertThat(out.toString(), containsString("Running story "
					+ storyPath));
		}
		assertThat(out.toString(),
				containsString("Failed to run batch stories"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotRenderReportsWhenRunningStoriesAsPathsIfRenderReportsAfterStoriesFlagIsNotSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doRenderReportsAfterStories(false);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		StoryConfiguration configuration = embedder.storyConfiguration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class storyClass : storyClasses) {
			storyPaths.add(resolver.resolve(storyClass));
		}
		embedder.runStoriesAsPaths(storyPaths);

		// Then
		for (String storyPath : storyPaths) {
			verify(runner).run(configuration, candidateSteps, storyPath);
			assertThat(out.toString(), containsString("Running story "
					+ storyPath));
		}
		assertThat(out.toString(), not(containsString("Rendering reports")));
		assertThat(out.toString(), not(containsString("Reports rendered")));
	}

	@Test
	public void shouldRenderReportsViaGivenRenderer() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doRenderReportsAfterStories(false);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		embedder.useReportRenderer(renderer);

		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerReportRenderer
				.defaultResources();
		when(renderer.countScenarios()).thenReturn(2);
		when(renderer.countFailedScenarios()).thenReturn(0);
		embedder.renderReports(outputDirectory, formats, renderingResources);

		// Then
		verify(renderer).render(outputDirectory, formats, renderingResources);
		assertThat(out.toString(), containsString("Rendering reports"));
		assertThat(out.toString(), containsString("Reports rendered"));
	}

	@Test
	public void shouldNotRenderReportsIfSkipFlagIsSet() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doSkip(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		embedder.useReportRenderer(renderer);

		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerReportRenderer
				.defaultResources();
		embedder.renderReports(outputDirectory, formats, renderingResources);

		// Then
		verify(renderer, never()).render(outputDirectory, formats,
				renderingResources);
		assertThat(out.toString(), not(containsString("Rendering reports")));
		assertThat(out.toString(), not(containsString("Reports rendered")));
	}

	@Test(expected = RenderingReportsFailedException.class)
	public void shouldThrowExceptionIfRenderingFails() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		embedder.useReportRenderer(renderer);
		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerReportRenderer
				.defaultResources();
		doThrow(new RuntimeException()).when(renderer).render(outputDirectory,
				formats, renderingResources);
		embedder.renderReports(outputDirectory, formats, renderingResources);

		// Then fail as expected
	}

	@Test(expected = RunningStoriesFailedException.class)
	public void shouldThrowExceptionIfScenariosFailedAndIgnoreFlagIsNotSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		embedder.useReportRenderer(renderer);
		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerReportRenderer
				.defaultResources();
		when(renderer.countScenarios()).thenReturn(2);
		when(renderer.countFailedScenarios()).thenReturn(1);
		embedder.renderReports(outputDirectory, formats, renderingResources);

		// Then fail as expected
	}

	@Test
	public void shouldNotThrowExceptionIfScenariosFailedAndIgnoreFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration().doIgnoreFailureInReports(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		embedder.useReportRenderer(renderer);
		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerReportRenderer
				.defaultResources();
		when(renderer.countScenarios()).thenReturn(2);
		when(renderer.countFailedScenarios()).thenReturn(1);
		embedder.renderReports(outputDirectory, formats, renderingResources);

		// Then 
		verify(renderer).render(outputDirectory, formats,
				renderingResources);
		assertThat(out.toString(), containsString("Reports rendered"));

	}

	@Test
	public void shouldAllowOverrideOfDefaultDependencies() throws Throwable {
		// Given
		StoryRunner runner = new StoryRunner();
		EmbedderConfiguration embedderConfiguration = new EmbedderConfiguration();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor();

		// When
		Embedder embedder = new Embedder(runner, embedderConfiguration, monitor);
		assertThat(embedder.storyRunner(), is(sameInstance(runner)));
		assertThat(embedder.embedderConfiguration(), is(sameInstance(embedderConfiguration)));
		assertThat(embedder.embedderMonitor(), is(sameInstance(monitor)));
		embedder.useStoryRunner(new StoryRunner());
		embedder.useEmbedderConfiguration(new EmbedderConfiguration());
		embedder.useEmbedderMonitor(new PrintStreamEmbedderMonitor());

		// Then
		assertThat(embedder.storyRunner(), is(not(sameInstance(runner))));
		assertThat(embedder.embedderConfiguration(), is(not(sameInstance(embedderConfiguration))));
		assertThat(embedder.embedderMonitor(), is(not(sameInstance(monitor))));
	}

	@Test
	public void shouldGenerateStepdoc() {
		// When
		Embedder embedder = new Embedder();
		embedder.generateStepdoc();
	}

	private class MyStory extends JUnitStory {
	}

	private class MyOtherStory extends JUnitStory {
	}

}
