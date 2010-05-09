package org.jbehave.core;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jbehave.core.parser.StoryPathResolver;
import org.jbehave.core.reporters.FreemarkerReportRenderer;
import org.jbehave.core.reporters.ReportRenderer;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;

public class StoryEmbedder {
    private StoryRunner runner;
    private StoryRunnerMode runnerMode;
    private StoryRunnerMonitor runnerMonitor;

    public StoryEmbedder() {
        this(new StoryRunner(), new StoryRunnerMode(), new PrintStreamRunnerMonitor());
    }

    public StoryEmbedder(StoryRunner runner, StoryRunnerMode runnerMode, StoryRunnerMonitor runnerMonitor) {
        this.runner = runner;
        this.runnerMonitor = runnerMonitor;
        this.runnerMode = runnerMode;
    }

    public void runStories(List<RunnableStory> runnableStories) {
        if (runnerMode.skip()) {
            runnerMonitor.storiesNotRun();
            return;
        }

        Map<String, Throwable> failedStories = new HashMap<String, Throwable>();
        for (RunnableStory story : runnableStories) {
            String storyName = story.getClass().getName();
            try {
                runnerMonitor.runningStory(storyName);
                story.run();
            } catch (Throwable e) {
                if (runnerMode.batch()) {
                    // collect and postpone decision to throw exception
                    failedStories.put(storyName, e);
                } else {
                    if (runnerMode.ignoreFailureInStories()) {
                        runnerMonitor.storyFailed(storyName, e);
                    } else {
                        throw new RunningStoriesFailedException("Failed to run story " + storyName, e);
                    }
                }
            }
        }

        if (runnerMode.batch() && failedStories.size() > 0) {
            if (runnerMode.ignoreFailureInStories()) {
                runnerMonitor.storiesBatchFailed(format(failedStories));
            } else {
                throw new RunningStoriesFailedException("Failed to run stories in batch: " + format(failedStories));
            }
        }
        
        if (runnerMode.renderReportsAfterStories()){
        	renderReports();
        }

    }

    public void runStoriesAsClasses(List<? extends Class<? extends RunnableStory>> storyClasses) {
        List<String> storyPaths = new ArrayList<String>();
        StoryPathResolver resolver = configuration().storyPathResolver();
        for (Class<? extends RunnableStory> storyClass : storyClasses) {
            storyPaths.add(resolver.resolve(storyClass));
        }
        runStoriesAsPaths(storyPaths);
    }

    public void runStoriesAsPaths(List<String> storyPaths) {
        if (runnerMode.skip()) {
            runnerMonitor.storiesNotRun();
            return;
        }

        Map<String, Throwable> failedStories = new HashMap<String, Throwable>();
        for (String storyPath : storyPaths) {
            try {
                runnerMonitor.runningStory(storyPath);
                StoryConfiguration configuration = configuration();
                runner.run(configuration, candidateSteps(), storyPath);
            } catch (Throwable e) {
                if (runnerMode.batch()) {
                    // collect and postpone decision to throw exception
                    failedStories.put(storyPath, e);
                } else {
                    if (runnerMode.ignoreFailureInStories()) {
                        runnerMonitor.storyFailed(storyPath, e);
                    } else {
                        throw new RunningStoriesFailedException("Failed to run story " + storyPath, e);
                    }
                }
            }
        }

        if (runnerMode.batch() && failedStories.size() > 0) {
            if (runnerMode.ignoreFailureInStories()) {
                runnerMonitor.storiesBatchFailed(format(failedStories));
            } else {
                throw new RunningStoriesFailedException("Failed to run stories in batch: " + format(failedStories));
            }
        }
        
        if (runnerMode.renderReportsAfterStories()){
        	renderReports();
        }

    }

	public void renderReports() {
		StoryReporterBuilder builder = configuration().storyReporterBuilder();
		File outputDirectory = builder.outputDirectory();
		List<String> formatNames = builder.formatNames(true);
		Properties renderingResources = builder.renderingResources();
		renderReports(outputDirectory, formatNames, renderingResources);		
	}

	public void renderReports(File outputDirectory, List<String> formats, Properties templateProperties) {
		if ( runnerMode.skip() ){
			runnerMonitor.reportsNotRendered();
			return;
		}
        ReportRenderer reportRenderer = new FreemarkerReportRenderer(templateProperties);
        try {
        	runnerMonitor.renderingReports(outputDirectory, formats, templateProperties);
            reportRenderer.render(outputDirectory, formats);
        } catch (RuntimeException e) {
        	runnerMonitor.reportRenderingFailed(outputDirectory, formats, templateProperties, e);
            String message = "Failed to render reports to "+outputDirectory+" with formats "+formats+" and template properties "+templateProperties;
			throw new RenderingReportsFailedException(message, e);
        }
        int scenarios = reportRenderer.countScenarios();
        int failedScenarios = reportRenderer.countFailedScenarios();
    	runnerMonitor.reportsRendered(scenarios, failedScenarios);
        if ( !runnerMode.ignoreFailureInReports() && failedScenarios > 0 ){
        	String message = "Rendered reports with "+scenarios+" scenarios (of which "+failedScenarios+" failed)";
			throw new RunningStoriesFailedException(message);
        }        
        
	}

    public void generateStepdoc() {
        StoryConfiguration configuration = configuration();
        List<CandidateSteps> candidateSteps = candidateSteps();
        configuration.stepdocReporter().report(configuration.stepdocGenerator().generate(candidateSteps.toArray(new CandidateSteps[candidateSteps.size()])));
    }
    
    public StoryConfiguration configuration() {
        return new MostUsefulStoryConfiguration();
    }

    public List<CandidateSteps> candidateSteps() {
        return asList(new CandidateSteps[]{});
    }

    public StoryRunnerMode runnerMode() {
        return runnerMode;
    }

    public void useStoryRunner(StoryRunner runner) {
        this.runner = runner;
    }

    public void useRunnerMode(StoryRunnerMode runnerMode) {
        this.runnerMode = runnerMode;
    }

    public void useRunnerMonitor(StoryRunnerMonitor runnerMonitor) {
        this.runnerMonitor = runnerMonitor;
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

    @SuppressWarnings("serial")
	private class RunningStoriesFailedException extends RuntimeException {
        public RunningStoriesFailedException(String message, Throwable cause) {
            super(message, cause);
        }

        public RunningStoriesFailedException(String message) {
            super(message);
        }
    }

    @SuppressWarnings("serial")
	private class RenderingReportsFailedException extends RuntimeException {
        public RenderingReportsFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
