package org.jbehave.core.runner;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

/**
 * Monitor that reports to {@link PrintStream} only failure events
 */
public class ReportingFailuresRunnerMonitor extends PrintStreamRunnerMonitor {

	public ReportingFailuresRunnerMonitor() {
        this(System.out);
    }

    public ReportingFailuresRunnerMonitor(PrintStream output) {
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
