package org.jbehave.core.embedder;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.ReportsCount;

/**
 * Decorator of EmbedderMonitor that delegates to an injected instance and
 * allows classes extending it to override only the methods that are needed.
 */
public class EmbedderMonitorDecorator implements EmbedderMonitor {

    private final EmbedderMonitor delegate;

    public EmbedderMonitorDecorator(EmbedderMonitor delegate) {
        this.delegate = delegate;
    }

    public void runningEmbeddable(String name) {
        delegate.runningEmbeddable(name);
    }

    public void embeddableFailed(String name, Throwable cause) {
        delegate.embeddableFailed(name, cause);
    }

    public void embeddableNotConfigurable(String name) {
        delegate.embeddableNotConfigurable(name);
    }

    public void embeddablesSkipped(List<String> classNames) {
        delegate.embeddablesSkipped(classNames);
    }

    public void metaNotAllowed(Meta meta, MetaFilter filter) {
        delegate.metaNotAllowed(meta, filter);
    }

    public void runningStory(String path) {
        delegate.runningStory(path);
    }

    public void storyFailed(String path, Throwable cause) {
        delegate.storyFailed(path, cause);
    }

    public void storiesSkipped(List<String> storyPaths) {
        delegate.storiesSkipped(storyPaths);
    }

    public void batchFailed(BatchFailures failures) {
        delegate.batchFailed(failures);
    }
    
    public void beforeOrAfterStoriesFailed() {
        delegate.beforeOrAfterStoriesFailed();
    }

    public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        delegate.generatingReportsView(outputDirectory, formats, viewProperties);
    }

    public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
        delegate.reportsViewGenerationFailed(outputDirectory, formats, viewProperties, cause);
    }

    public void reportsViewGenerated(ReportsCount count) {
        delegate.reportsViewGenerated(count);
    }

    public void reportsViewNotGenerated() {
        delegate.reportsViewNotGenerated();
    }

    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        delegate.annotatedInstanceNotOfType(annotatedInstance, type);
    }

    public void mappingStory(String storyPath, List<String> metaFilters) {
        delegate.mappingStory(storyPath, metaFilters);
    }

    public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
        delegate.generatingMapsView(outputDirectory, storyMaps, viewProperties);
    }

    public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
            Throwable cause) {
        delegate.mapsViewGenerationFailed(outputDirectory, storyMaps, viewProperties, cause);
    }

    public void generatingNavigatorView(File outputDirectory, Properties viewResources) {
        delegate.generatingNavigatorView(outputDirectory, viewResources);
    }

    public void navigatorViewGenerationFailed(File outputDirectory, Properties viewResources, Throwable cause) {
        delegate.navigatorViewGenerationFailed(outputDirectory, viewResources, cause);
    }

    public void navigatorViewNotGenerated() {
        delegate.navigatorViewNotGenerated();        
    }

    public void processingSystemProperties(Properties properties) {
        delegate.processingSystemProperties(properties);
    }

    public void systemPropertySet(String name, String value) {
        delegate.systemPropertySet(name, value);
    }
    
    public void storyTimeout(long durationInSecs, Story story) {
        delegate.storyTimeout(durationInSecs, story);
    }

    public void usingThreads(int threads) {
        delegate.usingThreads(threads);
    }

}
