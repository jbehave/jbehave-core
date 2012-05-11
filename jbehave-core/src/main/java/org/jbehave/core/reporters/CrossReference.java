package org.jbehave.core.reporters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.jbehave.core.embedder.MatchingStepMonitor.StepMatch;
import org.jbehave.core.embedder.PerformableTree.PerformableExampleScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableRoot;
import org.jbehave.core.embedder.PerformableTree.PerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.TableTransformers.FromLandscape;
import org.jbehave.core.steps.AbstractStepResult.Failed;
import org.jbehave.core.steps.AbstractStepResult.Ignorable;
import org.jbehave.core.steps.AbstractStepResult.NotPerformed;
import org.jbehave.core.steps.AbstractStepResult.Pending;
import org.jbehave.core.steps.AbstractStepResult.Silent;
import org.jbehave.core.steps.AbstractStepResult.Skipped;
import org.jbehave.core.steps.AbstractStepResult.Successful;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehave.core.steps.StepMonitor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class CrossReference {

    private boolean doJson = true;
    private boolean doXml = true;
    private String name;

    public CrossReference() {
        this("XREF");
    }

    public CrossReference(String name) {
        this.name = name;
    }

    public CrossReference withJsonOnly() {
        doJson = true;
        doXml = false;
        return this;
    }

    public CrossReference withXmlOnly() {
        doJson = false;
        doXml = true;
        return this;
    }

    public synchronized void serialise(Object root, File outputDirectory) {
        if (doXml) {
            serialise(root, "xml", outputDirectory);
        }
        if (doJson) {
            serialise(root, "json", outputDirectory);
        }
    }

    private void serialise(Object object, String format, File outputDirectory) {
        try {
            serialise(object, xstream(format), writer(format, outputDirectory));
        } catch (IOException e) {
            throw new RuntimeException(name, e);
        }
    }

    private void serialise(Object object, XStream xstream, Writer writer) throws IOException {
        writer.write(xstream.toXML(object));
        writer.flush();
        writer.close();
    }

    private Writer writer(String format, File outputDirectory) throws IOException {
        String name = fileName(format);
        File outputDir = new File(outputDirectory, "view");
        outputDir.mkdirs();
        return new FileWriter(new File(outputDir, name));
    }

    private XStream xstream(String format) {
        XStream xstream = (format.equals("json") ? new XStream(new JsonHierarchicalStreamDriver()) : new XStream());
        configure(xstream);
        return xstream;
    }

    private void configure(XStream xstream) {
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias(name.toLowerCase(), PerformableRoot.class);
        xstream.alias("performableStory", PerformableStory.class);
        xstream.alias("performableScenario", PerformableScenario.class);
        xstream.alias("performableExample", PerformableExampleScenario.class);
        xstream.alias("story", Story.class);
        xstream.alias("scenario", Scenario.class);
        xstream.alias("givenStory", GivenStory.class);
        xstream.alias("failed", Failed.class);
        xstream.alias("pending", Pending.class);
        xstream.alias("notPerformed", NotPerformed.class);
        xstream.alias("successful", Successful.class);
        xstream.alias("ignorable", Ignorable.class);
        xstream.alias("silent", Silent.class);
        xstream.alias("skipped", Skipped.class);
        xstream.alias("fromLandscape", FromLandscape.class);
        xstream.alias("stepMatch", StepMatch.class);
        xstream.omitField(ExamplesTable.class, "parameterConverters");
        xstream.omitField(ExamplesTable.class, "tableTrasformers");
        xstream.omitField(ExamplesTable.class, "defaults");
    }

    private String fileName(String extension) {
        return name.toLowerCase() + "." + extension;
    }

    /**
     * @deprecated
     */
    public synchronized void outputToFiles(StoryReporterBuilder storyReporterBuilder) {
    }

    /**
     * @deprecated
     */
    public CrossReference withMetaFilter(String metaFilter) {
        return this;
    }

    /**
     * @deprecated
     */
    public CrossReference withPendingStepStrategy(PendingStepStrategy pendingStepStrategy) {
        return this;
    }

    /**
     * @deprecated
     */
    public CrossReference withOutputAfterEachStory(boolean outputAfterEachStory) {
        return this;
    }

    /**
     * @deprecated
     */
    public CrossReference withThreadSafeDelegateFormat(Format format) {
        return this;
    }

    /**
     * @deprecated
     */
    public CrossReference excludingStoriesWithNoExecutedScenarios(boolean exclude) {
        return this;
    }

    /**
     * @deprecated
     */
    public String getMetaFilter() {
        return "";
    }

    /**
     * @deprecated
     */
    public StepMonitor getStepMonitor() {
        return new NullStepMonitor();
    }

    /**
     * @deprecated
     */
    protected XRefRoot newXRefRoot() {
        return new XRefRoot();
    }

    /**
     * @deprecated
     */
    protected Writer makeWriter(File file) throws IOException {
        return new FileWriter(file);
    }

    /**
     * @deprecated
     */
    protected void aliasForXRefStory(XStream xstream) {
        xstream.alias("story", XRefStory.class);
    }

    /**
     * @deprecated
     */
    protected void aliasForXRefRoot(XStream xstream) {
        xstream.alias("xref", XRefRoot.class);
    }

    /**
     * @deprecated
     */
    public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
            final StoryReporterBuilder storyReporterBuilder) {
        return new DelegatingStoryReporter(new NullStoryReporter());
    }

    /**
     * @deprecated
     */
    public static class XRefRoot {
     }

    /**
     * @deprecated
     */
    public static class XRefStory {
    }


}
