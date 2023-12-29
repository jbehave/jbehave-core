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
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null Object
 * Pattern</a> implementation of {@link EmbedderMonitor}. Can be extended to
 * override only the methods of interest.
 */
public class NullEmbedderMonitor implements EmbedderMonitor {

    @Override
    public void runningEmbeddable(String name) {
        // Do nothing by default
    }

    @Override
    public void embeddableFailed(String name, Throwable cause) {
        // Do nothing by default
    }

    @Override
    public void embeddableNotConfigurable(String name) {
        // Do nothing by default
    }

    @Override
    public void embeddablesSkipped(List<String> classNames) {
        // Do nothing by default
    }

    @Override
    public void metaExcluded(Meta meta, MetaFilter filter) {
        // Do nothing by default
    }

    @Override
    public void runningStory(String path) {
        // Do nothing by default
    }

    @Override
    public void storyFailed(String path, Throwable cause) {
        // Do nothing by default
    }

    @Override
    public void storiesSkipped(List<String> storyPaths) {
        // Do nothing by default
    }

    @Override
    public void storiesExcluded(List<Story> excluded, MetaFilter filter, boolean verbose) {
        // Do nothing by default
    }

    @Override
    public void scenarioExcluded(Scenario scenario, MetaFilter filter) {
        // Do nothing by default
    }

    @Override
    public void batchFailed(BatchFailures failures) {
        // Do nothing by default
    }

    @Override
    public void beforeOrAfterStoriesFailed() {
        // Do nothing by default
    }

    @Override
    public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        // Do nothing by default
    }

    @Override
    public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
        // Do nothing by default
    }

    @Override
    public void reportsViewGenerated(ReportsCount count) {
        // Do nothing by default
    }

    @Override
    public void reportsViewFailures(ReportsCount count) {
        // Do nothing by default
    }

    @Override
    public void reportsViewNotGenerated() {
        // Do nothing by default
    }

    @Override
    public void runningWithAnnotatedEmbedderRunner(String className) {
        // Do nothing by default
    }

    @Override
    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        // Do nothing by default
    }

    @Override
    public void mappingStory(String storyPath, List<String> metaFilters) {
        // Do nothing by default
    }

    @Override
    public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
        // Do nothing by default
    }

    @Override
    public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
            Throwable cause) {
        // Do nothing by default
    }

    @Override
    public void processingSystemProperties(Properties properties) {
        // Do nothing by default
    }

    @Override
    public void systemPropertySet(String name, String value) {
        // Do nothing by default
    }

    @Override
    public void storyTimeout(Story story, StoryDuration storyDuration) {
        // Do nothing by default
    }

    @Override
    public void usingThreads(int threads) {
        // Do nothing by default
    }

    @Override
    public void usingExecutorService(ExecutorService executorService) {
        // Do nothing by default
    }

    @Override
    public void usingControls(EmbedderControls embedderControls) {
        // Do nothing by default
    }

    @Override
    public void usingTimeout(String path, long timeout) {
        // Do nothing by default
    }
}
