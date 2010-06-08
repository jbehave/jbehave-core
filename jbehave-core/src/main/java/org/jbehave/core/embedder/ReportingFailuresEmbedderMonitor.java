package org.jbehave.core.embedder;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

/**
 * Monitor that reports to {@link PrintStream} only failure events
 */
public class ReportingFailuresEmbedderMonitor extends PrintStreamEmbedderMonitor {

	public ReportingFailuresEmbedderMonitor() {
        this(System.out);
    }

    public ReportingFailuresEmbedderMonitor(PrintStream output) {
        super(output);
    }

    public void runningStory(String storyName) {
    }

	public void renderingReports(File outputDirectory, List<String> formats,
			Properties templateProperties) {
	}
	
	public void reportsRendered(int scenarios, int failedScenarios) {
	}
	
}
