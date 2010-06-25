package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
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
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder.RenderingReportsFailedException;
import org.jbehave.core.embedder.Embedder.RunningStoriesFailedException;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.reporters.FreemarkerViewGenerator;
import org.jbehave.core.reporters.PrintStreamStepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Steps;
import org.junit.Test;

public class EmbedderBehaviour {

	@Test
	public void shouldRunStoriesAsRunnables() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderControls embedderControls = new EmbedderControls();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		// When
		Configuration configuration = new MostUsefulConfiguration();
		CandidateSteps steps = mock(CandidateSteps.class);
		Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
		assertThatStoriesViewGenerated(out);
	}

	private void assertThatStoriesViewGenerated(OutputStream out) {
		assertThat(out.toString(), containsString("Generating stories view"));
		assertThat(out.toString(), containsString("Stories view generated"));
	}

	@Test
	public void shouldNotRunStoriesAsRunnablesIfSkipFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderControls embedderControls = new EmbedderControls().doSkip(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		embedder.configuration().useStoryPathResolver(new UnderscoredCamelCaseResolver());
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
		EmbedderControls embedderControls = new EmbedderControls();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
		EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInStories(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
		EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
		EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
		EmbedderControls embedderControls = new EmbedderControls().doBatch(true).doIgnoreFailureInStories(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
		EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		RunnableStory myStory = mock(RunnableStory.class);
		RunnableStory myOtherStory = mock(RunnableStory.class);
		List<RunnableStory> runnables = asList(myStory, myOtherStory);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
		EmbedderControls embedderControls = new EmbedderControls();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		embedder.runStoriesAsClasses(storyClasses);

		// Then
		Configuration configuration = embedder.configuration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		for (Class<? extends RunnableStory> storyClass : storyClasses) {
			String storyPath = resolver.resolve(storyClass);
			verify(runner).run(configuration, candidateSteps, storyPath);
			assertThat(out.toString(), containsString("Running story "
					+ storyPath));
		}
		assertThatStoriesViewGenerated(out);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldRunStoriesAsPaths() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderControls embedderControls = new EmbedderControls();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		Configuration configuration = embedder.configuration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class<? extends RunnableStory> storyClass : storyClasses) {
			String storyPath = resolver.resolve(storyClass);
            storyPaths.add(storyPath);
	        StoryReporter storyReporter = mock(StoryReporter.class);
	        configuration.useStoryReporter(storyPath, storyReporter);
	        assertThat(configuration.storyReporter(storyPath), sameInstance(storyReporter));
		}
		embedder.runStoriesAsPaths(storyPaths);

		// Then
		for (String storyPath : storyPaths) {
			verify(runner).run(configuration, candidateSteps, storyPath);
			assertThat(out.toString(), containsString("Running story "
					+ storyPath));
		}
		assertThatStoriesViewGenerated(out);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotRunStoriesIfSkipFlagIsSet() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderControls embedderControls = new EmbedderControls().doSkip(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		Configuration configuration = embedder.configuration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class<? extends RunnableStory> storyClass : storyClasses) {
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
		EmbedderControls embedderControls = new EmbedderControls();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		Configuration configuration = embedder.configuration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class<? extends RunnableStory> storyClass : storyClasses) {
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
		EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInStories(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		Configuration configuration = embedder.configuration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class<? extends RunnableStory> storyClass : storyClasses) {
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
		EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		Configuration configuration = embedder.configuration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class<? extends RunnableStory> storyClass : storyClasses) {
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
		EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		Configuration configuration = embedder.configuration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class<? extends RunnableStory> storyClass : storyClasses) {
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
		EmbedderControls embedderControls = new EmbedderControls().doBatch(true).doIgnoreFailureInStories(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		Configuration configuration = embedder.configuration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class<? extends RunnableStory> storyClass : storyClasses) {
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
		EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		List<? extends Class<? extends RunnableStory>> storyClasses = asList(
				MyStory.class, MyOtherStory.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		Configuration configuration = embedder.configuration();
		List<CandidateSteps> candidateSteps = embedder.candidateSteps();
		StoryPathResolver resolver = configuration.storyPathResolver();
		List<String> storyPaths = new ArrayList<String>();
		for (Class<? extends RunnableStory> storyClass : storyClasses) {
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
		EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ViewGenerator viewGenerator = mock(ViewGenerator.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		embedder.configuration().useViewGenerator(viewGenerator);

		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerViewGenerator
				.defaultResources();
		when(viewGenerator.countScenarios()).thenReturn(2);
		when(viewGenerator.countFailedScenarios()).thenReturn(0);
		embedder.generateStoriesView(outputDirectory, formats, renderingResources);

		// Then
		verify(viewGenerator).generateView(outputDirectory, formats, renderingResources);
		assertThatStoriesViewGenerated(out);
	}

	@Test
	public void shouldNotRenderReportsIfSkipFlagIsSet() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderControls embedderControls = new EmbedderControls().doSkip(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ViewGenerator viewGenerator = mock(ViewGenerator.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.configuration().useStoryReporterBuilder(new StoryReporterBuilder().withDefaultFormats());
		embedder.configuration().useViewGenerator(viewGenerator);

		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerViewGenerator
				.defaultResources();
		embedder.generateStoriesView(outputDirectory, formats, renderingResources);

		// Then
		verify(viewGenerator, never()).generateView(outputDirectory, formats,
				renderingResources);
		assertThat(out.toString(), not(containsString("Rendering reports")));
		assertThat(out.toString(), not(containsString("Reports rendered")));
	}

	@Test(expected = RenderingReportsFailedException.class)
	public void shouldThrowExceptionIfRenderingFails() throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderControls embedderControls = new EmbedderControls();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ViewGenerator viewGenerator = mock(ViewGenerator.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		embedder.configuration().useViewGenerator(viewGenerator);
		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerViewGenerator
				.defaultResources();
		doThrow(new RuntimeException()).when(viewGenerator).generateView(outputDirectory,
				formats, renderingResources);
		embedder.generateStoriesView(outputDirectory, formats, renderingResources);

		// Then fail as expected
	}

	@Test(expected = RunningStoriesFailedException.class)
	public void shouldThrowExceptionIfScenariosFailedAndIgnoreFlagIsNotSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderControls embedderControls = new EmbedderControls();
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ViewGenerator viewGenerator = mock(ViewGenerator.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		embedder.configuration().useViewGenerator(viewGenerator);
		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerViewGenerator
				.defaultResources();
		when(viewGenerator.countScenarios()).thenReturn(2);
		when(viewGenerator.countFailedScenarios()).thenReturn(1);
		embedder.generateStoriesView(outputDirectory, formats, renderingResources);

		// Then fail as expected
	}

	@Test
	public void shouldNotThrowExceptionIfScenariosFailedAndIgnoreFlagIsSet()
			throws Throwable {
		// Given
		StoryRunner runner = mock(StoryRunner.class);
		EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInView(true);
		OutputStream out = new ByteArrayOutputStream();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(
				new PrintStream(out));
		ViewGenerator viewGenerator = mock(ViewGenerator.class);

		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		embedder.configuration().useViewGenerator(viewGenerator);
		File outputDirectory = new File("target/output");
		List<String> formats = asList("html");
		Properties renderingResources = FreemarkerViewGenerator
				.defaultResources();
		when(viewGenerator.countScenarios()).thenReturn(2);
		when(viewGenerator.countFailedScenarios()).thenReturn(1);
		embedder.generateStoriesView(outputDirectory, formats, renderingResources);

		// Then 
		verify(viewGenerator).generateView(outputDirectory, formats,
				renderingResources);
		assertThatStoriesViewGenerated(out);
	}

	@Test
	public void shouldAllowOverrideOfDefaultDependencies() throws Throwable {
		// Given
		StoryRunner runner = new StoryRunner();
		EmbedderControls embedderControls = new EmbedderControls();
		EmbedderMonitor monitor = new PrintStreamEmbedderMonitor();

		// When
		Embedder embedder = embedderWith(runner, embedderControls, monitor);
		assertThat(embedder.configuration().embedderControls(), is(sameInstance(embedderControls)));
		assertThat(embedder.storyRunner(), is(sameInstance(runner)));
		assertThat(embedder.embedderMonitor(), is(sameInstance(monitor)));
		embedder.useStoryRunner(new StoryRunner());
		embedder.useEmbedderMonitor(new PrintStreamEmbedderMonitor());

		// Then
		assertThat(embedder.storyRunner(), is(not(sameInstance(runner))));
		assertThat(embedder.embedderMonitor(), is(not(sameInstance(monitor))));
	}

	private Embedder embedderWith(StoryRunner runner,
			EmbedderControls embedderControls, EmbedderMonitor monitor) {
		Embedder embedder = new Embedder(runner, monitor);
		embedder.useEmbedderControls(embedderControls);
		return embedder;
	}

	@Test
	public void shouldFindAndReportMatchingSteps() {
		// Given
		Embedder embedder = new Embedder();
		embedder.useCandidateSteps(asList((CandidateSteps)new MySteps()));
		embedder.configuration().useStepFinder(new StepFinder());
		OutputStream out = new ByteArrayOutputStream();
		embedder.configuration().useStepdocReporter(new PrintStreamStepdocReporter(new PrintStream(out)));
		// When
		embedder.reportMatchingStepdocs("Given a given");
		// Then
		String expected = 
			"Step 'Given a given' is matched by annotated patterns:\n" +
			"'Given a given'\n" +
			"org.jbehave.core.embedder.EmbedderBehaviour$MySteps.given()\n" +
			"from steps instances:\n" +
			"org.jbehave.core.embedder.EmbedderBehaviour$MySteps\n";
		assertThat(out.toString(), equalTo(expected));
	}
	
	@Test
	public void shouldReportNoMatchingStepdocsFoundWithStepProvided() {
		// Given
		Embedder embedder = new Embedder();
		embedder.useCandidateSteps(asList((CandidateSteps)new MySteps()));
		embedder.configuration().useStepFinder(new StepFinder());
		OutputStream out = new ByteArrayOutputStream();
		embedder.configuration().useStepdocReporter(new PrintStreamStepdocReporter(new PrintStream(out)));
		// When
		embedder.reportMatchingStepdocs("Given a non-defined step");
		// Then
		String expected = 
			"Step 'Given a non-defined step' is not matched by any pattern\n" +
			"from steps instances:\n" +
			"org.jbehave.core.embedder.EmbedderBehaviour$MySteps\n";
		assertThat(out.toString(), equalTo(expected));
	}

	@Test
	public void shouldReportNoMatchingStepdocsFoundWhenNoStepsProvided() {
		// Given
		Embedder embedder = new Embedder();
		embedder.useCandidateSteps(asList(new CandidateSteps[]{}));
		embedder.configuration().useStepFinder(new StepFinder());
		OutputStream out = new ByteArrayOutputStream();
		embedder.configuration().useStepdocReporter(new PrintStreamStepdocReporter(new PrintStream(out)));
		// When
		embedder.reportMatchingStepdocs("Given a non-defined step");
		// Then
		String expected = 
			"Step 'Given a non-defined step' is not matched by any pattern\n" +
			"as no steps instances are provided\n";
		assertThat(out.toString(), equalTo(expected));
	}

	@Test
	public void shouldReportAllStepdocs() {
		// Given
		Embedder embedder = new Embedder();
		embedder.useCandidateSteps(asList((CandidateSteps)new MySteps()));
		embedder.configuration().useStepFinder(new StepFinder());
		OutputStream out = new ByteArrayOutputStream();
		embedder.configuration().useStepdocReporter(new PrintStreamStepdocReporter(new PrintStream(out)));
		// When
		embedder.reportStepdocs();
		// Then
		String expected = 
			"'Given a given'\n"+
			"org.jbehave.core.embedder.EmbedderBehaviour$MySteps.given()\n"+
			"'When a when'\n"+
			"org.jbehave.core.embedder.EmbedderBehaviour$MySteps.when()\n"+
			"'Then a then'\n"+
			"org.jbehave.core.embedder.EmbedderBehaviour$MySteps.then()\n"+
			"from steps instances:\norg.jbehave.core.embedder.EmbedderBehaviour$MySteps\n";
		assertThat(out.toString(), equalTo(expected));
	}
	
	private class MyStory extends JUnitStory {
	}

	private class MyOtherStory extends JUnitStory {
	}

    public static class MySteps extends Steps {
        
        @Given("a given")
        public void given() {
        }

        @When("a when")
        public void when() {
        }
        
        @Then("a then")
        public void then() {
        }
                
    }
}
