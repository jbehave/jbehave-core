package org.jbehave.core;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

public class PrintStreamRunnerMonitor implements StoryRunnerMonitor {
    private PrintStream output;

    public PrintStreamRunnerMonitor() {
        this(System.out);
    }

    public PrintStreamRunnerMonitor(PrintStream output) {
        this.output = output;
    }

    public void storiesBatchFailed(String failedStories) {
        output.println("Failed to run batch stories "+ failedStories);
    }

    public void storyFailed(String storyName, Throwable e) {
        output.println("Failed to run story "+storyName);
        e.printStackTrace(output);
    }

    public void runningStory(String storyName) {
        output.println("Running story "+storyName);
    }

    public void storiesNotRun() {
        output.println("Stories not run");
    }

	public void renderingReports(File outputDirectory, List<String> formats,
			Properties templateProperties) {
		output.println("Rendering reports in '" + outputDirectory + "' using formats '" + formats + "'" 
    		    + " and template properties '"+templateProperties+"'");
	}

	public void reportRenderingFailed(File outputDirectory,
			List<String> formats, Properties templateProperties, Throwable cause) {
		output.println("Failed to render reports in outputDirectory " + outputDirectory
        		+ " using formats " + formats + " and template properties '"+templateProperties+"'");
	}
}
