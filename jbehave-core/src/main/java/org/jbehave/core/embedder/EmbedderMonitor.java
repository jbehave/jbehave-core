package org.jbehave.core.embedder;

import java.io.File;
import java.util.List;
import java.util.Properties;


public interface EmbedderMonitor {

    void runningStory(String storyName);

    void storyFailed(String storyName, Throwable cause);

    void storiesNotRun();

    void storiesBatchFailed(String failedStories);

	void generatingStoriesView(File outputDirectory, List<String> formats,
			Properties viewProperties);

	void storiesViewGenerationFailed(File outputDirectory, List<String> formats,
			Properties viewProperties, Throwable cause);

	void storiesViewGenerated(int stories, int scenarios, int failedScenarios);

	void storiesViewNotGenerated();

    void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type);
    
}
