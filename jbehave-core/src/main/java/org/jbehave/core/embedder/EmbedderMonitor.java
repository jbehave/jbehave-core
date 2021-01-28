package org.jbehave.core.embedder;

import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.*;
import org.jbehave.core.reporters.ReportsCount;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public interface EmbedderMonitor {

    void runningEmbeddable(String name);

    void embeddableFailed(String name, Throwable cause);

    void embeddableNotConfigurable(String name);

    void embeddablesSkipped(List<String> classNames);

    void metaNotAllowed(Meta meta, MetaFilter filter);

    void runningStory(String path);

    void storyFailed(String path, Throwable cause);

    void storiesSkipped(List<String> storyPaths);

    void storiesNotAllowed(List<Story> notAllowed, MetaFilter filter, boolean verbose);

	void scenarioNotAllowed(Scenario scenario, MetaFilter filter);

    void batchFailed(BatchFailures failures);

    void beforeOrAfterStoriesFailed();

    void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties);

    void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause);

    void reportsViewGenerated(ReportsCount count);

    void reportsViewFailures(ReportsCount count);

    void reportsViewNotGenerated();

    void runningWithAnnotatedEmbedderRunner(String className);

    void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type);

    void mappingStory(String storyPath, List<String> metaFilters);

    void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties);

    void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties, Throwable cause);

    void processingSystemProperties(Properties properties);

    void systemPropertySet(String name, String value);

    void storyTimeout(Story story, StoryDuration storyDuration);

    void usingThreads(int threads);

    void usingExecutorService(ExecutorService executorService);

    void usingControls(EmbedderControls embedderControls);
    
    void invalidTimeoutFormat(String path);
    
    void usingTimeout(String path, long timeout);

}
