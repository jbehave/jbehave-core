package org.jbehave.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.parser.StoryPathResolver;
import org.jbehave.core.reporters.FreemarkerReportRenderer;
import org.jbehave.core.reporters.ReportRenderer;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.runner.PrintStreamRunnerMonitor;
import org.jbehave.core.runner.StoryRunner;
import org.jbehave.core.runner.StoryRunnerMode;
import org.jbehave.core.runner.StoryRunnerMonitor;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepdocGenerator;

public class StoryEmbedder {
    private StoryConfiguration configuration = new MostUsefulStoryConfiguration();
    private List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
    private ReportRenderer reportRenderer = new FreemarkerReportRenderer();
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
                story.useEmbedder(this);
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

	public void renderReports(File outputDirectory, List<String> formats, Properties renderingResources) {
		if ( runnerMode.skip() ){
			runnerMonitor.reportsNotRendered();
			return;
		}
        ReportRenderer reportRenderer = reportRenderer();
        try {
        	runnerMonitor.renderingReports(outputDirectory, formats, renderingResources);
            reportRenderer.render(outputDirectory, formats, renderingResources);
        } catch (RuntimeException e) {
        	runnerMonitor.reportRenderingFailed(outputDirectory, formats, renderingResources, e);
            String message = "Failed to render reports to "+outputDirectory+" with formats "+formats+" and rendering resources "+renderingResources;
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
        StepdocReporter reporter = configuration.stepdocReporter();
		StepdocGenerator generator = configuration.stepdocGenerator();
		reporter.report(generator.generate(candidateSteps.toArray(new CandidateSteps[candidateSteps.size()])));
    }
    
    public StoryConfiguration configuration() {
        return configuration;
    }

    public List<CandidateSteps> candidateSteps() {
		return candidateSteps;
    }
    
	public ReportRenderer reportRenderer() {
		return reportRenderer;
	}

    public StoryRunnerMode runnerMode() {
        return runnerMode;
    }

	public StoryRunnerMonitor runnerMonitor() {
		return runnerMonitor;
	}

    public StoryRunner storyRunner() {
		return runner;
	}
    
	public void useConfiguration(StoryConfiguration configuration) {
		this.configuration = configuration;
	}

	public void useCandidateSteps(List<CandidateSteps> candidateSteps) {
		this.candidateSteps = candidateSteps;		
	}
	
	public void useReportRenderer(ReportRenderer reportRenderer){
		this.reportRenderer = reportRenderer;		
	}

	public void useRunnerMode(StoryRunnerMode runnerMode) {
        this.runnerMode = runnerMode;
    }

    public void useRunnerMonitor(StoryRunnerMonitor runnerMonitor) {
        this.runnerMonitor = runnerMonitor;
    }

    public void useStoryRunner(StoryRunner runner) {
        this.runner = runner;
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
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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
