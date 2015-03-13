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

    public void runningEmbeddable(String name) {
    }

    public void embeddableFailed(String name, Throwable cause) {
    }

    public void embeddableNotConfigurable(String name) {
    }

    public void embeddablesSkipped(List<String> classNames) {
    }

    public void metaNotAllowed(Meta meta, MetaFilter filter) {
    }

    public void runningStory(String path) {
    }

    public void storyFailed(String path, Throwable cause) {
    }

    public void storiesSkipped(List<String> storyPaths) {
    }

    public void storiesNotAllowed(List<Story> notAllowed, MetaFilter filter) {
        storiesNotAllowed(notAllowed, filter, false);
    }

    public void storiesNotAllowed(List<Story> notAllowed, MetaFilter filter, boolean verbose) {
    }

	public void scenarioNotAllowed(Scenario scenario, MetaFilter filter) {
	}

    public void batchFailed(BatchFailures failures) {
    }

    public void beforeOrAfterStoriesFailed() {
    }

    public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
    }

    public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
    }

    public void reportsViewGenerated(ReportsCount count) {
    }

    public void reportsViewFailures(ReportsCount count) {
    }

    public void reportsViewNotGenerated() {
    }

    public void runningWithAnnotatedEmbedderRunner(String className) {
    }

    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
    }

    public void mappingStory(String storyPath, List<String> metaFilters) {
    }

    public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
    }

    public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
            Throwable cause) {
    }

    public void generatingNavigatorView(File outputDirectory, Properties viewResources) {
    }

    public void navigatorViewGenerationFailed(File outputDirectory, Properties viewResources, Throwable cause) {
    }

    public void navigatorViewNotGenerated() {
    }

    public void processingSystemProperties(Properties properties) {
    }

    public void systemPropertySet(String name, String value) {
    }

    public void storyTimeout(Story story, StoryDuration storyDuration) {
    }

    public void usingThreads(int threads) {
    }

    public void usingExecutorService(ExecutorService executorService) {
    }

    public void usingControls(EmbedderControls embedderControls) {
    }

	public String getSearchDirectory() {
		return null;
	}

	public void invalidTimeoutFormat(String path) {
	}

	public void usingTimeout(String path, long timeout) {
	}

}
