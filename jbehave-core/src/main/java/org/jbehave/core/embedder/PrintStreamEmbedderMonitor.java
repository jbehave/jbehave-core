package org.jbehave.core.embedder;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Meta;

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

    public void embeddablesSkipped(List<String> classNames) {
        print("Skipped embeddables "+classNames);        
    }
    
    public void metaNotAllowed(Meta meta, MetaFilter filter) {
        print(meta +" not allowed by filter '"+filter.asString()+"'");        
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
        print("Skipped stories "+storyPaths);        
    }
    
    public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
        print("Annotated instance " + annotatedInstance + " if not of type " + type);
    }

    public void generatingStoriesView(File outputDirectory, List<String> formats, Properties viewProperties) {
        print("Generating stories view in '" + outputDirectory + "' using formats '" + formats + "'"
                + " and view properties '" + viewProperties + "'");
    }

    public void storiesViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
            Throwable cause) {
        print("Failed to generate stories view in outputDirectory " + outputDirectory + " using formats " + formats
                + " and view properties '" + viewProperties + "'");
    }

    public void storiesViewGenerated(int stories, int scenarios, int failedScenarios) {
        print("Stories view generated with " + stories + " stories containing " + scenarios + " scenarios (of which  "
                + failedScenarios + " failed)");
    }

    public void storiesViewNotGenerated() {
        print("Stories view not generated");
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
