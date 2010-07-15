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
    
    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        print("Annotated instance "+annotatedInstance+" if not of type "+type);
    }

	public void generatingStoriesView(File outputDirectory, List<String> formats,
			Properties viewProperties) {
		print("Generating stories view in '" + outputDirectory + "' using formats '" + formats + "'" 
    		    + " and view properties '"+viewProperties+"'");
	}

	public void storiesViewGenerationFailed(File outputDirectory,
			List<String> formats, Properties viewProperties, Throwable cause) {
		print("Failed to generate stories view in outputDirectory " + outputDirectory
        		+ " using formats " + formats + " and view properties '"+viewProperties+"'");
	}
	
	public void storiesViewGenerated(int scenarios, int failedScenarios) {
		print("Stories view generated with " + scenarios
        		+ " scenarios (of which  " + failedScenarios + " failed)");
	}
	
	public void storiesViewNotGenerated() {
		print("Stories view not generated");
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
