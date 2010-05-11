package org.jbehave.core;

import java.io.File;
import java.util.List;
import java.util.Properties;


public interface StoryRunnerMonitor {

    void runningStory(String storyName);

    void storyFailed(String storyName, Throwable e);

    void storiesNotRun();

    void storiesBatchFailed(String failedStories);

	void renderingReports(File outputDirectory, List<String> formats,
			Properties templateProperties);

	void reportRenderingFailed(File outputDirectory, List<String> formats,
			Properties templateProperties, Throwable cause);

	void reportsRendered(int scenarios, int failedScenarios);

	void reportsNotRendered();
    
}
