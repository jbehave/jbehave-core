package org.jbehave.core.embedder;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
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

    @Override
    public void runningEmbeddable(String name) {
        delegate.runningEmbeddable(name);
    }

    @Override
    public void embeddableFailed(String name, Throwable cause) {
        delegate.embeddableFailed(name, cause);
    }

    @Override
    public void embeddableNotConfigurable(String name) {
        delegate.embeddableNotConfigurable(name);
    }

    @Override
    public void embeddablesSkipped(List<String> classNames) {
        delegate.embeddablesSkipped(classNames);
    }

    @Override
    public void metaExcluded(Meta meta, MetaFilter filter) {
        delegate.metaExcluded(meta, filter);
    }

    @Override
    public void runningStory(String path) {
        delegate.runningStory(path);
    }

    @Override
    public void storyFailed(String path, Throwable cause) {
        delegate.storyFailed(path, cause);
    }

    @Override
    public void storiesSkipped(List<String> storyPaths) {
        delegate.storiesSkipped(storyPaths);
    }

    @Override
    public void storiesExcluded(List<Story> excluded, MetaFilter filter, boolean verbose) {
        delegate.storiesExcluded(excluded, filter, verbose);
     }

    @Override
    public void scenarioExcluded(Scenario scenario, MetaFilter filter) {
        delegate.scenarioExcluded(scenario, filter);
    }

    @Override
    public void batchFailed(BatchFailures failures) {
        delegate.batchFailed(failures);
    }
    
    @Override
    public void beforeOrAfterStoriesFailed() {
        delegate.beforeOrAfterStoriesFailed();
    }

    @Override
    public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        delegate.generatingReportsView(outputDirectory, formats, viewProperties);
    }

    @Override
    public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
        delegate.reportsViewGenerationFailed(outputDirectory, formats, viewProperties, cause);
    }

    @Override
    public void reportsViewGenerated(ReportsCount count) {
        delegate.reportsViewGenerated(count);
    }

    @Override
    public void reportsViewFailures(ReportsCount count) {
        delegate.reportsViewFailures(count);
    }

    @Override
    public void reportsViewNotGenerated() {
        delegate.reportsViewNotGenerated();
    }
    
    @Override
    public void runningWithAnnotatedEmbedderRunner(String className) {
        delegate.runningWithAnnotatedEmbedderRunner(className);
    }

    @Override
    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        delegate.annotatedInstanceNotOfType(annotatedInstance, type);
    }

    @Override
    public void mappingStory(String storyPath, List<String> metaFilters) {
        delegate.mappingStory(storyPath, metaFilters);
    }

    @Override
    public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
        delegate.generatingMapsView(outputDirectory, storyMaps, viewProperties);
    }

    @Override
    public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
            Throwable cause) {
        delegate.mapsViewGenerationFailed(outputDirectory, storyMaps, viewProperties, cause);
    }

    @Override
    public void processingSystemProperties(Properties properties) {
        delegate.processingSystemProperties(properties);
    }

    @Override
    public void systemPropertySet(String name, String value) {
        delegate.systemPropertySet(name, value);
    }
    
    @Override
    public void storyTimeout(Story story, StoryDuration storyDuration) {
        delegate.storyTimeout(story, storyDuration);
    }

    @Override
    public void usingThreads(int threads) {
        delegate.usingThreads(threads);
    }

    @Override
    public void usingExecutorService(ExecutorService executorService) {
        delegate.usingExecutorService(executorService);
    }

    @Override
    public void usingControls(EmbedderControls embedderControls) {
        delegate.usingControls(embedderControls);        
    }

    @Override
    public void invalidTimeoutFormat(String path) {
        delegate.invalidTimeoutFormat(path);
    }

    @Override
    public void usingTimeout(String path, long timeout) {
        delegate.usingTimeout(path, timeout);
    }

}
