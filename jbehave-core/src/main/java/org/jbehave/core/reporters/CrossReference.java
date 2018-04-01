package org.jbehave.core.reporters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.embedder.MatchingStepMonitor.StepMatch;
import org.jbehave.core.embedder.PerformableTree.ExamplePerformableScenario;
import org.jbehave.core.embedder.PerformableTree.NormalPerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableRoot;
import org.jbehave.core.embedder.PerformableTree.PerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableSteps;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.embedder.PerformableTree.Status;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.model.*;
import org.jbehave.core.model.TableTransformers.Formatting;
import org.jbehave.core.model.TableTransformers.FromLandscape;
import org.jbehave.core.model.TableTransformers.Replacing;
import org.jbehave.core.steps.AbstractStepResult.Comment;
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
import org.jbehave.core.steps.Timing;

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

	public synchronized void serialise(PerformableRoot root,
			File outputDirectory) {
		XRef xref = new XRef(root);
		if (doXml) {
			serialise(xref, "xml", outputDirectory);
		}
		if (doJson) {
			serialise(xref, "json", outputDirectory);
		}
	}

	private void serialise(Object object, String format, File outputDirectory) {
		try {
			serialise(object, xstream(format), writer(format, outputDirectory));
		} catch (IOException e) {
			throw new RuntimeException(name, e);
		}
	}

	private void serialise(Object object, XStream xstream, Writer writer)
			throws IOException {
		writer.write(xstream.toXML(object));
		writer.flush();
		writer.close();
	}

	private Writer writer(String format, File outputDirectory)
			throws IOException {
		String name = fileName(format);
		File outputDir = new File(outputDirectory, "view");
		outputDir.mkdirs();
		return new FileWriter(new File(outputDir, name));
	}

	private XStream xstream(String format) {
		XStream xstream = (format.equals("json") ? new XStream(
				new JsonHierarchicalStreamDriver()) : new XStream());
		configure(xstream);
		return xstream;
	}

	private void configure(XStream xstream) {
		xstream.setMode(XStream.NO_REFERENCES);
		xstream.alias("xref", XRef.class);
		xstream.alias(name.toLowerCase(), PerformableRoot.class);
		xstream.alias("performableStory", PerformableStory.class);
		xstream.alias("performableScenario", PerformableScenario.class);
		xstream.alias("performableSteps", PerformableSteps.class);
		xstream.alias("normalPerformableScenario", NormalPerformableScenario.class);
		xstream.alias("examplePerformableScenario", ExamplePerformableScenario.class);
		xstream.alias("status", Status.class);
		xstream.alias("story", Story.class);
		xstream.alias("scenario", Scenario.class);
		xstream.alias("lifecycleSteps", Lifecycle.Steps.class);
		xstream.alias("givenStory", GivenStory.class);
		xstream.alias("comment", Comment.class);
		xstream.alias("failed", Failed.class);
		xstream.alias("pending", Pending.class);
		xstream.alias("notPerformed", NotPerformed.class);
		xstream.alias("successful", Successful.class);
		xstream.alias("ignorable", Ignorable.class);
		xstream.alias("silent", Silent.class);
		xstream.alias("skipped", Skipped.class);
		xstream.alias("fromLandscape", FromLandscape.class);
		xstream.alias("formatting", Formatting.class);
		xstream.alias("replacing", Replacing.class);
		xstream.alias("stepMatch", StepMatch.class);
		xstream.alias("timing", Timing.class);
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
	public synchronized void outputToFiles(
			StoryReporterBuilder storyReporterBuilder) {
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
	public CrossReference withPendingStepStrategy(
			PendingStepStrategy pendingStepStrategy) {
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
	public CrossReference excludingStoriesWithNoExecutedScenarios(
			boolean exclude) {
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
	}

	/**
	 * @deprecated
	 */
	protected void aliasForXRefRoot(XStream xstream) {
	}

	/**
	 * @deprecated
	 */
	public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
			final StoryReporterBuilder storyReporterBuilder) {
		return new NullStoryReporter();
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

	public static class XRef {
		private List<PerformableStory> stories;
		private List<PerformableScenario> scenarios = new ArrayList<>();

		public XRef(PerformableRoot root) {
			stories = root.getStories();
			for (PerformableStory story : stories) {
				scenarios.addAll(story.getScenarios());
			}
		}

	}

}
