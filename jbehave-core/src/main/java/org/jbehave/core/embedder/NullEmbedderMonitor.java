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
    }

    @Override
    public void embeddableFailed(String name, Throwable cause) {
    }

    @Override
    public void embeddableNotConfigurable(String name) {
    }

    @Override
    public void embeddablesSkipped(List<String> classNames) {
    }

    @Override
    public void metaNotAllowed(Meta meta, MetaFilter filter) {
    }

    @Override
    public void runningStory(String path) {
    }

    @Override
    public void storyFailed(String path, Throwable cause) {
    }

    @Override
    public void storiesSkipped(List<String> storyPaths) {
    }

    @Override
    public void storiesNotAllowed(List<Story> notAllowed, MetaFilter filter) {
        storiesNotAllowed(notAllowed, filter, false);
    }

    @Override
    public void storiesNotAllowed(List<Story> notAllowed, MetaFilter filter, boolean verbose) {
    }

	@Override
    public void scenarioNotAllowed(Scenario scenario, MetaFilter filter) {
	}

    @Override
    public void batchFailed(BatchFailures failures) {
    }

    @Override
    public void beforeOrAfterStoriesFailed() {
    }

    @Override
    public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
    }

    @Override
    public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
    }

    @Override
    public void reportsViewGenerated(ReportsCount count) {
    }

    @Override
    public void reportsViewFailures(ReportsCount count) {
    }

    @Override
    public void reportsViewNotGenerated() {
    }

    @Override
    public void runningWithAnnotatedEmbedderRunner(String className) {
    }

    @Override
    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
    }

    @Override
    public void mappingStory(String storyPath, List<String> metaFilters) {
    }

    @Override
    public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
    }

    @Override
    public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
            Throwable cause) {
    }

    @Override
    public void processingSystemProperties(Properties properties) {
    }

    @Override
    public void systemPropertySet(String name, String value) {
    }

    @Override
    public void storyTimeout(Story story, StoryDuration storyDuration) {
    }

    @Override
    public void usingThreads(int threads) {
    }

    @Override
    public void usingExecutorService(ExecutorService executorService) {
    }

    @Override
    public void usingControls(EmbedderControls embedderControls) {
    }

	public String getSearchDirectory() {
		return null;
	}

	@Override
    public void invalidTimeoutFormat(String path) {
	}

	@Override
    public void usingTimeout(String path, long timeout) {
	}

}
