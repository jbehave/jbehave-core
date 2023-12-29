package org.jbehave.core.embedder;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.Collection;
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
 * Monitor which collects other {@link EmbedderMonitor}s and delegates all invocations to the collected monitors.
 */
public class DelegatingEmbedderMonitor implements EmbedderMonitor {

    private final Collection<EmbedderMonitor> delegates;

    /**
     * Creates {@link DelegatingEmbedderMonitor} with a given collections of delegates
     *
     * @param delegates the {@link EmbedderMonitor}-s to delegate to
     */
    public DelegatingEmbedderMonitor(Collection<EmbedderMonitor> delegates) {
        this.delegates = delegates;
    }

    /**
     * Creates {@link DelegatingEmbedderMonitor} with a given varargs of delegates
     *
     * @param delegates the {@link EmbedderMonitor}-s to delegate to
     */
    public DelegatingEmbedderMonitor(EmbedderMonitor... delegates) {
        this(asList(delegates));
    }

    @Override
    public void runningEmbeddable(String name) {
        delegates.forEach(d -> d.runningEmbeddable(name));
    }

    @Override
    public void embeddableFailed(String name, Throwable cause) {
        delegates.forEach(d -> d.embeddableFailed(name, cause));
    }

    @Override
    public void embeddableNotConfigurable(String name) {
        delegates.forEach(d -> d.embeddableNotConfigurable(name));
    }

    @Override
    public void embeddablesSkipped(List<String> classNames) {
        delegates.forEach(d -> d.embeddablesSkipped(classNames));
    }

    @Override
    public void metaExcluded(Meta meta, MetaFilter filter) {
        delegates.forEach(d -> d.metaExcluded(meta, filter));
    }

    @Override
    public void runningStory(String path) {
        delegates.forEach(d -> d.runningStory(path));
    }

    @Override
    public void storyFailed(String path, Throwable cause) {
        delegates.forEach(d -> d.storyFailed(path, cause));
    }

    @Override
    public void storiesSkipped(List<String> storyPaths) {
        delegates.forEach(d -> d.storiesSkipped(storyPaths));
    }

    @Override
    public void storiesExcluded(List<Story> excluded, MetaFilter filter, boolean verbose) {
        delegates.forEach(d -> d.storiesExcluded(excluded, filter, verbose));
    }

    @Override
    public void scenarioExcluded(Scenario scenario, MetaFilter filter) {
        delegates.forEach(d -> d.scenarioExcluded(scenario, filter));
    }

    @Override
    public void batchFailed(BatchFailures failures) {
        delegates.forEach(d -> d.batchFailed(failures));
    }

    @Override
    public void beforeOrAfterStoriesFailed() {
        delegates.forEach(EmbedderMonitor::beforeOrAfterStoriesFailed);
    }

    @Override
    public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        delegates.forEach(d -> d.generatingReportsView(outputDirectory, formats, viewProperties));
    }

    @Override
    public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
        delegates.forEach(d -> d.reportsViewGenerationFailed(outputDirectory, formats, viewProperties, cause));
    }

    @Override
    public void reportsViewGenerated(ReportsCount count) {
        delegates.forEach(d -> d.reportsViewGenerated(count));
    }

    @Override
    public void reportsViewFailures(ReportsCount count) {
        delegates.forEach(d -> d.reportsViewFailures(count));
    }

    @Override
    public void reportsViewNotGenerated() {
        delegates.forEach(EmbedderMonitor::reportsViewNotGenerated);
    }

    @Override
    public void runningWithAnnotatedEmbedderRunner(String className) {
        delegates.forEach(d -> d.runningWithAnnotatedEmbedderRunner(className));
    }

    @Override
    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        delegates.forEach(d -> d.annotatedInstanceNotOfType(annotatedInstance, type));
    }

    @Override
    public void mappingStory(String storyPath, List<String> metaFilters) {
        delegates.forEach(d -> d.mappingStory(storyPath, metaFilters));
    }

    @Override
    public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
        delegates.forEach(d -> d.generatingMapsView(outputDirectory, storyMaps, viewProperties));
    }

    @Override
    public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
            Throwable cause) {
        delegates.forEach(d -> d.mapsViewGenerationFailed(outputDirectory, storyMaps, viewProperties, cause));
    }

    @Override
    public void processingSystemProperties(Properties properties) {
        delegates.forEach(d -> d.processingSystemProperties(properties));
    }

    @Override
    public void systemPropertySet(String name, String value) {
        delegates.forEach(d -> d.systemPropertySet(name, value));
    }

    @Override
    public void storyTimeout(Story story, StoryDuration storyDuration) {
        delegates.forEach(d -> d.storyTimeout(story, storyDuration));
    }

    @Override
    public void usingThreads(int threads) {
        delegates.forEach(d -> d.usingThreads(threads));
    }

    @Override
    public void usingExecutorService(ExecutorService executorService) {
        delegates.forEach(d -> d.usingExecutorService(executorService));
    }

    @Override
    public void usingControls(EmbedderControls embedderControls) {
        delegates.forEach(d -> d.usingControls(embedderControls));
    }

    @Override
    public void usingTimeout(String path, long timeout) {
        delegates.forEach(d -> d.usingTimeout(path, timeout));
    }
}
