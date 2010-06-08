package org.jbehave.core.embedder;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Monitor that reports to a {@link PrintStream}, defaulting to {@link System.out}
 */
public class PrintStreamEmbedderMonitor implements EmbedderMonitor {
    private PrintStream output;

    public PrintStreamEmbedderMonitor() {
        this(System.out);
    }

    public PrintStreamEmbedderMonitor(PrintStream output) {
        this.output = output;
    }

    public void storiesBatchFailed(String failedStories) {
        print("Failed to run batch stories "+ failedStories);
    }

    public void storyFailed(String storyName, Throwable e) {
        print("Failed to run story "+storyName);
        printStackTrace(e);
    }

    public void runningStory(String storyName) {
        print("Running story "+storyName);
    }

    public void storiesNotRun() {
        print("Stories not run");
    }

	public void renderingReports(File outputDirectory, List<String> formats,
			Properties templateProperties) {
		print("Rendering reports in '" + outputDirectory + "' using formats '" + formats + "'" 
    		    + " and template properties '"+templateProperties+"'");
	}

	public void reportRenderingFailed(File outputDirectory,
			List<String> formats, Properties templateProperties, Throwable cause) {
		print("Failed to render reports in outputDirectory " + outputDirectory
        		+ " using formats " + formats + " and template properties '"+templateProperties+"'");
	}
	
	public void reportsRendered(int scenarios, int failedScenarios) {
		print("Reports rendered with " + scenarios
        		+ " scenarios (of which  " + failedScenarios + " failed)");
	}
	
	public void reportsNotRendered() {
		print("Reports not rendered");
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	protected void print(String message) {
		output.println(message);
	}

	protected void printStackTrace(Throwable e) {
		e.printStackTrace(output);
	}


}
