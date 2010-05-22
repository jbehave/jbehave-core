package org.jbehave.core;

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

import org.jbehave.core.StoryEmbedder.RenderingReportsFailedException;
import org.jbehave.core.StoryEmbedder.RunningStoriesFailedException;
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.reporters.FreemarkerReportRenderer;
import org.jbehave.core.reporters.ReportRenderer;
import org.jbehave.core.runner.PrintStreamRunnerMonitor;
import org.jbehave.core.runner.StoryRunner;
import org.jbehave.core.runner.StoryRunnerMode;
import org.jbehave.core.runner.StoryRunnerMonitor;
import org.jbehave.core.steps.CandidateSteps;
import org.junit.Test;

public class StoryEmbedderBehaviour {

	@Test
	public void shouldRunStoriesAsRunnables() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		StoryRunnerMode mode = new StoryRunnerMode();
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		StoryConfiguration configuration = new MostUsefulStoryConfiguration();
		CandidateSteps steps = mock(CandidateSteps.class);
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(false, true, false, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, false, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, true, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(true, false, false, false,
				false);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(true, false, false, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(true, false, true, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, false, false,
				false);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode();
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		embedder.runStoriesAsClasses(storyClasses);

		// Then
		StoryConfiguration configuration = embedder.configuration();
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
		StoryRunnerMode mode = new StoryRunnerMode();
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		StoryConfiguration configuration = embedder.configuration();
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
		StoryRunnerMode mode = new StoryRunnerMode(false, true, false, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		StoryConfiguration configuration = embedder.configuration();
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, false, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		StoryConfiguration configuration = embedder.configuration();
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, true, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		StoryConfiguration configuration = embedder.configuration();
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
		StoryRunnerMode mode = new StoryRunnerMode(true, false, false, false,
				false);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		StoryConfiguration configuration = embedder.configuration();
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
		StoryRunnerMode mode = new StoryRunnerMode(true, false, false, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		StoryConfiguration configuration = embedder.configuration();
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
		StoryRunnerMode mode = new StoryRunnerMode(true, false, true, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		StoryConfiguration configuration = embedder.configuration();
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, false, false,
				false);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		StoryConfiguration configuration = embedder.configuration();
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, false, false,
				false);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(false, true, false, false,
				false);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, false, false,
				false);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, false, false,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode(false, false, false, true,
				true);
		OutputStream out = new ByteArrayOutputStream();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor(
				new PrintStream(out));
		ReportRenderer renderer = mock(ReportRenderer.class);

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
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
		StoryRunnerMode mode = new StoryRunnerMode();
		StoryRunnerMonitor monitor = new PrintStreamRunnerMonitor();

		// When
		StoryEmbedder embedder = new StoryEmbedder(runner, mode, monitor);
		assertThat(embedder.storyRunner(), is(sameInstance(runner)));
		assertThat(embedder.runnerMode(), is(sameInstance(mode)));
		assertThat(embedder.runnerMonitor(), is(sameInstance(monitor)));
		embedder.useStoryRunner(new StoryRunner());
		embedder.useRunnerMode(new StoryRunnerMode());
		embedder.useRunnerMonitor(new PrintStreamRunnerMonitor());

		// Then
		assertThat(embedder.storyRunner(), is(not(sameInstance(runner))));
		assertThat(embedder.runnerMode(), is(not(sameInstance(mode))));
		assertThat(embedder.runnerMonitor(), is(not(sameInstance(monitor))));
	}

	@Test
	public void shouldGenerateStepdoc() {
		// When
		StoryEmbedder embedder = new StoryEmbedder();
		embedder.generateStepdoc();
	}

	private class MyStory extends JUnitStory {
	}

	private class MyOtherStory extends JUnitStory {
	}

}
