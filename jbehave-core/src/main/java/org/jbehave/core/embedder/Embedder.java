package org.jbehave.core.embedder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Stepdoc;

/**
 * Represents an embeddable entry point to all of JBehave's functionality.
 */
public class Embedder {
	
	private Configuration configuration = new MostUsefulConfiguration();
	private List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
    private EmbedderControls embedderControls = new EmbedderControls();
	private StoryRunner storyRunner;
	private EmbedderMonitor embedderMonitor;

	public Embedder() {
		this(new StoryRunner(), new PrintStreamEmbedderMonitor());
	}

	public Embedder(StoryRunner storyRunner, EmbedderMonitor embedderMonitor) {
		this.storyRunner = storyRunner;
		this.embedderMonitor = embedderMonitor;
	}

	public void runStoriesAsEmbeddables(List<Embeddable> embeddableStories) {
		EmbedderControls embedderControls = embedderControls();
		if (embedderControls.skip()) {
			embedderMonitor.storiesNotRun();
			return;
		}

		Map<String, Throwable> failedStories = new HashMap<String, Throwable>();
		for (Embeddable story : embeddableStories) {
			String storyName = story.getClass().getName();
			try {
				embedderMonitor.runningStory(storyName);
				story.useEmbedder(this);
				story.run();
			} catch (Throwable e) {
				if (embedderControls.batch()) {
					// collect and postpone decision to throw exception
					failedStories.put(storyName, e);
				} else {
					if (embedderControls.ignoreFailureInStories()) {
						embedderMonitor.storyFailed(storyName, e);
					} else {
						throw new RunningStoriesFailedException(
								"Failed to run story " + storyName, e);
					}
				}
			}
		}

		if (embedderControls.batch() && failedStories.size() > 0) {
			if (embedderControls.ignoreFailureInStories()) {
				embedderMonitor.storiesBatchFailed(format(failedStories));
			} else {
				throw new RunningStoriesFailedException(
						"Failed to run stories in batch: "
								+ format(failedStories));
			}
		}

		if (embedderControls.generateViewAfterStories()) {
			generateStoriesView();
		}

	}

	public void runStoriesAsClasses(
			List<? extends Class<? extends Embeddable>> storyClasses) {
		List<String> storyPaths = new ArrayList<String>();
		StoryPathResolver resolver = configuration().storyPathResolver();
		for (Class<? extends Embeddable> storyClass : storyClasses) {
			storyPaths.add(resolver.resolve(storyClass));
		}
		runStoriesAsPaths(storyPaths);
	}
	
	public void buildReporters(Configuration configuration, List<String> storyPaths) {
		StoryReporterBuilder reporterBuilder = configuration.storyReporterBuilder();
		configuration.useStoryReporters(reporterBuilder.build(storyPaths));
	}

	public void runStoriesAsPaths(List<String> storyPaths) {
		EmbedderControls embedderControls = embedderControls();
		if (embedderControls.skip()) {
			embedderMonitor.storiesNotRun();
			return;
		}

		Map<String, Throwable> failedStories = new HashMap<String, Throwable>();
		Configuration configuration = configuration();
		buildReporters(configuration, storyPaths);
		for (String storyPath : storyPaths) {
			try {
				embedderMonitor.runningStory(storyPath);
				storyRunner.run(configuration, candidateSteps(), storyPath);
			} catch (Throwable e) {
				if (embedderControls.batch()) {
					// collect and postpone decision to throw exception
					failedStories.put(storyPath, e);
				} else {
					if (embedderControls.ignoreFailureInStories()) {
						embedderMonitor.storyFailed(storyPath, e);
					} else {
						throw new RunningStoriesFailedException(
								"Failed to run story " + storyPath, e);
					}
				}
			}
		}

		if (embedderControls.batch() && failedStories.size() > 0) {
			if (embedderControls.ignoreFailureInStories()) {
				embedderMonitor.storiesBatchFailed(format(failedStories));
			} else {
				throw new RunningStoriesFailedException(
						"Failed to run stories in batch: "
								+ format(failedStories));
			}
		}

		if (embedderControls.generateViewAfterStories()) {
			generateStoriesView();
		}

	}

	public void generateStoriesView() {
		StoryReporterBuilder builder = configuration().storyReporterBuilder();
		File outputDirectory = builder.outputDirectory();
		List<String> formatNames = builder.formatNames(true);
		generateStoriesView(outputDirectory, formatNames, builder.viewResources());
	}

	public void generateStoriesView(File outputDirectory, List<String> formats,
			Properties viewResources) {
		EmbedderControls embedderControls = embedderControls();

		if (embedderControls.skip()) {
			embedderMonitor.storiesViewNotGenerated();
			return;
		}
		ViewGenerator viewGenerator = configuration().viewGenerator();
		try {
			embedderMonitor.generatingStoriesView(outputDirectory, formats,
					viewResources);
			viewGenerator.generateView(outputDirectory, formats, viewResources);
		} catch (RuntimeException e) {
			embedderMonitor.storiesViewGenerationFailed(outputDirectory, formats,
					viewResources, e);
			String message = "Failed to generate stories view in " + outputDirectory
					+ " with formats " + formats + " and resources "
					+ viewResources;
			throw new RenderingReportsFailedException(message, e);
		}
		int scenarios = viewGenerator.countScenarios();
		int failedScenarios = viewGenerator.countFailedScenarios();
		embedderMonitor.storiesViewGenerated(scenarios, failedScenarios);
		if (!embedderControls.ignoreFailureInView() && failedScenarios > 0) {
			String message = "Generated stories view with " + scenarios
					+ " scenarios (of which " + failedScenarios + " failed)";
			throw new RunningStoriesFailedException(message);
		}

	}

	public void reportStepdocs() {
		Configuration configuration = configuration();
		List<CandidateSteps> candidateSteps = candidateSteps();
		StepFinder finder = configuration.stepFinder();
		StepdocReporter reporter = configuration.stepdocReporter();
		List<Object> stepsInstances = finder.stepsInstances(candidateSteps);
		reporter.stepdocs(finder.stepdocs(candidateSteps), stepsInstances);
	}

	public void reportMatchingStepdocs(String stepAsString){
		Configuration configuration = configuration();
		List<CandidateSteps> candidateSteps = candidateSteps();
		StepFinder finder = configuration.stepFinder();
		StepdocReporter reporter = configuration.stepdocReporter();
		List<Stepdoc> matching = finder.findMatching(stepAsString, candidateSteps);
		List<Object> stepsInstances = finder.stepsInstances(candidateSteps);
		reporter.stepdocsMatching(stepAsString, matching, stepsInstances);		
	}
	
	public Configuration configuration() {
		return configuration;
	}

	public List<CandidateSteps> candidateSteps() {
		return candidateSteps;
	}

	public EmbedderControls embedderControls() {
		return embedderControls;
	}
	
	public EmbedderMonitor embedderMonitor() {
		return embedderMonitor;
	}

	public StoryRunner storyRunner() {
		return storyRunner;
	}

	public void useConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void useCandidateSteps(List<CandidateSteps> candidateSteps) {
		this.candidateSteps = candidateSteps;
	}

	public void useEmbedderControls(EmbedderControls embedderControls) {
        this.embedderControls = embedderControls;		
	}

	public void useEmbedderMonitor(EmbedderMonitor embedderMonitor) {
		this.embedderMonitor = embedderMonitor;
	}

	public void useStoryRunner(StoryRunner storyRunner) {
		this.storyRunner = storyRunner;
	}

	private String format(Map<String, Throwable> failedStories) {
		StringBuffer sb = new StringBuffer();
		for (String storyName : failedStories.keySet()) {
			Throwable cause = failedStories.get(storyName);
			sb.append("\n");
			sb.append(storyName);
			sb.append(": ");
			sb.append(cause.getMessage());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@SuppressWarnings("serial")
	public class RunningStoriesFailedException extends RuntimeException {
		public RunningStoriesFailedException(String message, Throwable cause) {
			super(message, cause);
		}

		public RunningStoriesFailedException(String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	public class RenderingReportsFailedException extends RuntimeException {
		public RenderingReportsFailedException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
