package org.jbehave.core.embedder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.RunnableStory;
import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.reporters.FreemarkerReportRenderer;
import org.jbehave.core.reporters.ReportRenderer;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepdocGenerator;

/**
 * Represents an embeddable entry point to all of JBehave's functionality.
 */
public class Embedder {
    private StoryConfiguration storyConfiguration = new MostUsefulStoryConfiguration();
    private List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
    private ReportRenderer reportRenderer = new FreemarkerReportRenderer();
	private StoryRunner storyRunner;
    private EmbedderConfiguration embedderConfiguration;
    private EmbedderMonitor embedderMonitor;

    public Embedder() {
        this(new StoryRunner(), new EmbedderConfiguration(), new PrintStreamEmbedderMonitor());
    }

    public Embedder(StoryRunner storyRunner, EmbedderConfiguration embedderConfiguration, EmbedderMonitor embedderMonitor) {
        this.storyRunner = storyRunner;
        this.embedderMonitor = embedderMonitor;
        this.embedderConfiguration = embedderConfiguration;
    }

	public void runStories(List<RunnableStory> runnableStories) {
        if (embedderConfiguration.skip()) {
            embedderMonitor.storiesNotRun();
            return;
        }

        Map<String, Throwable> failedStories = new HashMap<String, Throwable>();
        for (RunnableStory story : runnableStories) {
            String storyName = story.getClass().getName();
            try {
                embedderMonitor.runningStory(storyName);
                story.useEmbedder(this);
                story.run();
            } catch (Throwable e) {
                if (embedderConfiguration.batch()) {
                    // collect and postpone decision to throw exception
                    failedStories.put(storyName, e);
                } else {
                    if (embedderConfiguration.ignoreFailureInStories()) {
                        embedderMonitor.storyFailed(storyName, e);
                    } else {
                        throw new RunningStoriesFailedException("Failed to run story " + storyName, e);
                    }
                }
            }
        }

        if (embedderConfiguration.batch() && failedStories.size() > 0) {
            if (embedderConfiguration.ignoreFailureInStories()) {
                embedderMonitor.storiesBatchFailed(format(failedStories));
            } else {
                throw new RunningStoriesFailedException("Failed to run stories in batch: " + format(failedStories));
            }
        }
        
        if (embedderConfiguration.renderReportsAfterStories()){
        	renderReports();
        }

    }

    public void runStoriesAsClasses(List<? extends Class<? extends RunnableStory>> storyClasses) {
        List<String> storyPaths = new ArrayList<String>();
        StoryPathResolver resolver = storyConfiguration().storyPathResolver();
        for (Class<? extends RunnableStory> storyClass : storyClasses) {
            storyPaths.add(resolver.resolve(storyClass));
        }
        runStoriesAsPaths(storyPaths);
    }

    public void runStoriesAsPaths(List<String> storyPaths) {
        if (embedderConfiguration.skip()) {
            embedderMonitor.storiesNotRun();
            return;
        }

        Map<String, Throwable> failedStories = new HashMap<String, Throwable>();
        for (String storyPath : storyPaths) {
            try {
                embedderMonitor.runningStory(storyPath);
                StoryConfiguration configuration = storyConfiguration();
                storyRunner.run(configuration, candidateSteps(), storyPath);
            } catch (Throwable e) {
                if (embedderConfiguration.batch()) {
                    // collect and postpone decision to throw exception
                    failedStories.put(storyPath, e);
                } else {
                    if (embedderConfiguration.ignoreFailureInStories()) {
                        embedderMonitor.storyFailed(storyPath, e);
                    } else {
                        throw new RunningStoriesFailedException("Failed to run story " + storyPath, e);
                    }
                }
            }
        }

        if (embedderConfiguration.batch() && failedStories.size() > 0) {
            if (embedderConfiguration.ignoreFailureInStories()) {
                embedderMonitor.storiesBatchFailed(format(failedStories));
            } else {
                throw new RunningStoriesFailedException("Failed to run stories in batch: " + format(failedStories));
            }
        }
        
        if (embedderConfiguration.renderReportsAfterStories()){
        	renderReports();
        }

    }

	public void renderReports() {
		StoryReporterBuilder builder = storyConfiguration().storyReporterBuilder();
		File outputDirectory = builder.outputDirectory();
		List<String> formatNames = builder.formatNames(true);
		Properties renderingResources = builder.renderingResources();
		renderReports(outputDirectory, formatNames, renderingResources);		
	}

	public void renderReports(File outputDirectory, List<String> formats, Properties renderingResources) {
		if ( embedderConfiguration.skip() ){
			embedderMonitor.reportsNotRendered();
			return;
		}
        ReportRenderer reportRenderer = reportRenderer();
        try {
        	embedderMonitor.renderingReports(outputDirectory, formats, renderingResources);
            reportRenderer.render(outputDirectory, formats, renderingResources);
        } catch (RuntimeException e) {
        	embedderMonitor.reportRenderingFailed(outputDirectory, formats, renderingResources, e);
            String message = "Failed to render reports to "+outputDirectory+" with formats "+formats+" and rendering resources "+renderingResources;
			throw new RenderingReportsFailedException(message, e);
        }
        int scenarios = reportRenderer.countScenarios();
        int failedScenarios = reportRenderer.countFailedScenarios();
    	embedderMonitor.reportsRendered(scenarios, failedScenarios);
        if ( !embedderConfiguration.ignoreFailureInReports() && failedScenarios > 0 ){
        	String message = "Rendered reports with "+scenarios+" scenarios (of which "+failedScenarios+" failed)";
			throw new RunningStoriesFailedException(message);
        }        
        
	}

    public void generateStepdoc() {
        StoryConfiguration configuration = storyConfiguration();
        List<CandidateSteps> candidateSteps = candidateSteps();
        StepdocReporter reporter = configuration.stepdocReporter();
		StepdocGenerator generator = configuration.stepdocGenerator();
		reporter.report(generator.generate(candidateSteps.toArray(new CandidateSteps[candidateSteps.size()])));
    }
    
    public StoryConfiguration storyConfiguration() {
        return storyConfiguration;
    }

    public List<CandidateSteps> candidateSteps() {
		return candidateSteps;
    }
    
	public ReportRenderer reportRenderer() {
		return reportRenderer;
	}

    public EmbedderConfiguration embedderConfiguration() {
        return embedderConfiguration;
    }

	public EmbedderMonitor embedderMonitor() {
		return embedderMonitor;
	}

    public StoryRunner storyRunner() {
		return storyRunner;
	}
    
	public void useConfiguration(StoryConfiguration configuration) {
		this.storyConfiguration = configuration;
	}

	public void useCandidateSteps(List<CandidateSteps> candidateSteps) {
		this.candidateSteps = candidateSteps;		
	}
	
	public void useReportRenderer(ReportRenderer reportRenderer){
		this.reportRenderer = reportRenderer;		
	}

	public void useEmbedderConfiguration(EmbedderConfiguration embedderConfiguration) {
        this.embedderConfiguration = embedderConfiguration;
    }

    public void useEmbedderMonitor(EmbedderMonitor embedderMonitor) {
        this.embedderMonitor = embedderMonitor;
    }

    public void useStoryRunner(StoryRunner runner) {
        this.storyRunner = runner;
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
