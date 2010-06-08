package org.jbehave.core.embedder;

import java.io.File;
import java.util.List;
import java.util.Properties;


public interface EmbedderMonitor {

    void runningStory(String storyName);

    void storyFailed(String storyName, Throwable cause);

    void storiesNotRun();

    void storiesBatchFailed(String failedStories);

	void renderingReports(File outputDirectory, List<String> formats,
			Properties templateProperties);

	void reportRenderingFailed(File outputDirectory, List<String> formats,
			Properties templateProperties, Throwable cause);

	void reportsRendered(int scenarios, int failedScenarios);

	void reportsNotRendered();
    
}
