package org.jbehave.core.embedder;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.ReportsCount;

/**
 * Monitor that reports to a {@link PrintStream}, defaulting to
 * {@link System.out}
 */
public class PrintStreamEmbedderMonitor implements EmbedderMonitor {
    private PrintStream output;

    public PrintStreamEmbedderMonitor() {
        this(System.out);
    }

    public PrintStreamEmbedderMonitor(PrintStream output) {
        this.output = output;
    }

    public void batchFailed(BatchFailures failures) {
        print("Failed to run batch " + failures);
    }

    public void embeddableFailed(String name, Throwable cause) {
        print("Failed to run embeddable " + name);
        printStackTrace(cause);
    }

    public void embeddableNotConfigurable(String name) {
        print("Embeddable " + name + " must be an instance of "+ConfigurableEmbedder.class);
    }

    public void embeddablesSkipped(List<String> classNames) {
        print("Skipped embeddables " + classNames);
    }

    public void metaNotAllowed(Meta meta, MetaFilter filter) {
        print(meta + " excluded by filter '" + filter.asString() + "'");
    }

    public void runningEmbeddable(String name) {
        print("Running embeddable " + name);
    }

    public void runningStory(String path) {
        print("Running story " + path);
    }

    public void storyFailed(String path, Throwable cause) {
        print("Failed to run story " + path);
        printStackTrace(cause);
    }

    public void storiesSkipped(List<String> storyPaths) {
        print("Skipped stories " + storyPaths);
    }

    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        print("Annotated instance " + annotatedInstance + " if not of type " + type);
    }

    public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        print("Generating reports view to '" + outputDirectory + "' using formats '" + formats + "'"
                + " and view properties '" + viewProperties + "'");
    }

    public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
        print("Failed to generate reports view to '" + outputDirectory + "' using formats '" + formats
                + "' and view properties '" + viewProperties + "'");
    }

    public void reportsViewGenerated(ReportsCount count) {
        print("Reports view generated with " + count.getStories() + " stories containing " + count.getScenarios() + " scenarios (of which  "
                + count.getScenariosFailed() + " failed)");
        if (count.getStoriesNotAllowed() > 0 || count.getScenariosNotAllowed() > 0) {
            print("Meta filters did not allow " + count.getStoriesNotAllowed() + " stories and  " + count.getScenariosNotAllowed()
                    + " scenarios");
        }
    }

    public void reportsViewNotGenerated() {
        print("Reports view not generated");
    }

    public void mappingStory(String storyPath, List<String> metaFilters) {
        print("Mapping story " + storyPath + " with meta filters " + metaFilters);
    }

    public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
        print("Generating maps view to '" + outputDirectory + "' using story maps '" + storyMaps + "'"
                + " and view properties '" + viewProperties + "'");
    }

    public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
            Throwable cause) {
        print("Failed to generating maps view to '" + outputDirectory + "' using story maps '" + storyMaps + "'"
                + " and view properties '" + viewProperties + "'");
        printStackTrace(cause);        
    }

    @Override
    public void generatingNavigatorView(File outputDirectory, Properties viewProperties) {
        print("Generating navigator view to '" + outputDirectory + "' using view properties '" + viewProperties + "'");
    }

    @Override
    public void navigatorViewGenerationFailed(File outputDirectory, Properties viewProperties, Throwable cause) {
        print("Failed to generating navigator view to '" + outputDirectory + "' using view properties '" + viewProperties + "'");
        printStackTrace(cause);        
    }

    public void navigatorViewNotGenerated() {
        print("Navigator view not generated, as the CrossReference has not been declared in the StoryReporterBuilder");
    }

    public void processingSystemProperties(Properties properties) {
        print("Processing system properties " + properties);
    }

    public void systemPropertySet(String name, String value) {
        print("System property '" + name + "' set to '"+value+"'");
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    protected void print(String message) {
        output.println(message);
    }

    protected void printStackTrace(Throwable e) {
        e.printStackTrace(output);
    }

}
