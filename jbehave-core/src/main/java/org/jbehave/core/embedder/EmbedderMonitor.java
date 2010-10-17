package org.jbehave.core.embedder;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Meta;



public interface EmbedderMonitor {

    void runningEmbeddable(String name);

    void embeddableFailed(String name, Throwable cause);
    
    void embeddablesSkipped(List<String> classNames);

    void metaNotAllowed(Meta meta, MetaFilter filter);
    
    void runningStory(String path);

    void storyFailed(String path, Throwable cause);

    void storiesSkipped(List<String> storyPaths);

    void batchFailed(BatchFailures failures);

	void generatingStoriesView(File outputDirectory, List<String> formats,
			Properties viewProperties);

	void storiesViewGenerationFailed(File outputDirectory, List<String> formats,
			Properties viewProperties, Throwable cause);

	void storiesViewGenerated(int stories, int scenarios, int failedScenarios);

	void storiesViewNotGenerated();

    void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type);

    void mappingStory(String storyPath, List<String> metaFilters);

 
}
