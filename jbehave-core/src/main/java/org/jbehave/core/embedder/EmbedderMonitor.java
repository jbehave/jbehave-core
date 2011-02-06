package org.jbehave.core.embedder;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.ReportsCount;

public interface EmbedderMonitor {

    void runningEmbeddable(String name);

    void embeddableFailed(String name, Throwable cause);

    void embeddablesSkipped(List<String> classNames);

    void metaNotAllowed(Meta meta, MetaFilter filter);

    void runningStory(String path);

    void storyFailed(String path, Throwable cause);

    void storiesSkipped(List<String> storyPaths);

    void batchFailed(BatchFailures failures);

    void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties);

    void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause);

    void reportsViewGenerated(ReportsCount count);

    void reportsViewNotGenerated();

    void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type);

    void mappingStory(String storyPath, List<String> metaFilters);

    void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties);

    void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties, Throwable cause);

    void generatingNavigatorView(File outputDirectory, Properties viewResources);

    void navigatorViewGenerationFailed(File outputDirectory, Properties viewResources, Throwable cause);

    void navigatorViewNotGenerated();

    void processingSystemProperties(Properties properties);

    void systemPropertySet(String name, String value);

}
