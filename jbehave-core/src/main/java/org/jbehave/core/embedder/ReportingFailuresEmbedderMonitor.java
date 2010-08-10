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

    @Override
    public void runningEmbeddable(String name) {
    }

    @Override
    public void runningStory(String path) {
    }

    @Override
	public void generatingStoriesView(File outputDirectory, List<String> formats,
			Properties viewProperties) {
	}
	
    @Override
	public void storiesViewGenerated(int stories, int scenarios, int failedScenarios) {
	}
	
}
