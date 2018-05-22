package org.jbehave.core.embedder;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.ReportsCount;

/**
 * Monitor that reports to a {@link PrintStream}, defaulting to
 * {@link System#out}
 */
public class PrintStreamEmbedderMonitor extends NullEmbedderMonitor {
    private PrintStream output;

    public PrintStreamEmbedderMonitor() {
        this(System.out);
    }

    public PrintStreamEmbedderMonitor(PrintStream output) {
        this.output = output;
    }

    @Override
    public void batchFailed(BatchFailures failures) {
        print("Failed to run batch %s", failures);
    }

    @Override
    public void beforeOrAfterStoriesFailed() {
        print("Failed to run before or after stories steps");
    }

    @Override
    public void embeddableFailed(String name, Throwable cause) {
        print("Failed to run embeddable %s", name);
        printStackTrace(cause);
    }

    @Override
    public void embeddableNotConfigurable(String name) {
        print("Embeddable %s must be an instance of %s", name,  ConfigurableEmbedder.class);
    }

    @Override
    public void embeddablesSkipped(List<String> classNames) {
        print("Skipped embeddables %s", classNames);
    }

    @Override
    public void metaNotAllowed(Meta meta, MetaFilter filter) {
        print("%s excluded by filter '%s'", meta, filter.asString());
    }

    @Override
    public void runningEmbeddable(String name) {
        print("Running embeddable %s", name);
    }

    @Override
    public void runningStory(String path) {
        print("Running story %s", path);
    }

    @Override
    public void storyFailed(String path, Throwable cause) {
        print("Failed to run story %s", path);
        printStackTrace(cause);
    }

    @Override
    public void storiesSkipped(List<String> storyPaths) {
        print("Skipped stories %s", storyPaths);
    }

    @Override
    public void storiesNotAllowed(List<Story> stories, MetaFilter filter, boolean verbose) {
        StringBuilder format = new StringBuilder("%d stories excluded by filter: %s%n");
        if (verbose) {
            for (Story story : stories) {
                format.append(story.getPath()).append("%n");
            }
        }
        print(format.toString(), stories.size(), filter.asString());
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, MetaFilter filter) {
        print("Scenario '%s' excluded by filter: %s%n", scenario.getTitle(), filter.asString());
    }

    @Override
    public void runningWithAnnotatedEmbedderRunner(String className) {
        print("Running with AnnotatedEmbedderRunner '%s'", className);
    }

    @Override
    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        print("Annotated instance %s if not of type %s", annotatedInstance, type);
    }

    @Override
    public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        print("Generating reports view to '%s' using formats '%s' and view properties '%s'", outputDirectory, formats,
                viewProperties);
    }

    @Override
    public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
        print("Failed to generate reports view to '%s' using formats '%s' and view properties '%s'", outputDirectory,
                formats, viewProperties);
    }

    @Override
    public void reportsViewGenerated(ReportsCount count) {
        print("Reports view generated with %d stories (of which %d pending) containing %d scenarios (of which %d pending)",
                count.getStories(), count.getStoriesPending(), count.getScenarios(), count.getScenariosPending());
        if (count.getStoriesNotAllowed() > 0 || count.getScenariosNotAllowed() > 0) {
            print("Meta filters excluded %d stories and  %d scenarios", count.getStoriesNotAllowed(),
                    count.getScenariosNotAllowed());
        }
    }

    @Override
    public void reportsViewFailures(ReportsCount count) {
        print("Failures in reports view: %d scenarios failed", count.getScenariosFailed());
    }

    @Override
    public void reportsViewNotGenerated() {
        print("Reports view not generated");
    }

    @Override
    public void mappingStory(String storyPath, List<String> metaFilters) {
        print("Mapping story %s with meta filters %s", storyPath, metaFilters);
    }

    @Override
    public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
        print("Generating maps view to '%s' using story maps '%s' and view properties '%s'", outputDirectory, storyMaps,
                viewProperties);
    }

    @Override
    public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
            Throwable cause) {
        print("Failed to generating maps view to '%s' using story maps '%s' and view properties '%s'", outputDirectory,
                storyMaps, viewProperties);
        printStackTrace(cause);
    }

    @Override
    public void generatingNavigatorView(File outputDirectory, Properties viewProperties) {
        print("Generating navigator view to '%s' using view properties '%s'", outputDirectory, viewProperties);
    }

    @Override
    public void navigatorViewGenerationFailed(File outputDirectory, Properties viewProperties, Throwable cause) {
        print("Failed to generating navigator view to '%s' using view properties '%s'", outputDirectory, viewProperties);
        printStackTrace(cause);
    }

    @Override
    public void navigatorViewNotGenerated() {
        print("Navigator view not generated, as the CrossReference has not been declared in the StoryReporterBuilder");
    }

    @Override
    public void processingSystemProperties(Properties properties) {
        print("Processing system properties %s", properties);
    }

    @Override
    public void systemPropertySet(String name, String value) {
        print("System property '%s' set to '%s'", name, value);
    }

    @Override
    public void storyTimeout(Story story, StoryDuration storyDuration) {
        print("Story %s duration of %d seconds has exceeded timeout of %d seconds", story.getPath(),
                storyDuration.getDurationInSecs(), storyDuration.getTimeoutInSecs());
    }

    @Override
    public void usingThreads(int threads) {
        print("Using %d threads", threads);
    }

    @Override
    public void usingExecutorService(ExecutorService executorService) {
        print("Using executor service %s", executorService);
    }

    @Override
    public void usingControls(EmbedderControls embedderControls) {
        print("Using controls %s", embedderControls);
    }
    
    
    @Override
    public void invalidTimeoutFormat(String path) {
        print("Failed to set specific story timeout for story %s because 'storyTimeoutInSecsByPath' has incorrect format",
                path);
        print("'storyTimeoutInSecsByPath' must be a CSV of regex expressions matching story paths. E.g. \"*/long/*.story:5000,*/short/*.story:200\"");
    }

    @Override
    public void usingTimeout(String path, long timeout) {
        print("Using timeout for story %s of %d secs.", path, timeout);
    }

     @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    protected void print(String format, Object... args) {
        Format.println(output, format, args);
    }

    protected void printStackTrace(Throwable e) {
        e.printStackTrace(output);
    }

}
