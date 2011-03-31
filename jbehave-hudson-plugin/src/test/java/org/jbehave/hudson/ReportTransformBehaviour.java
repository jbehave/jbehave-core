package org.jbehave.hudson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepFinder.ByLevenshteinDistance;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class ReportTransformBehaviour {
	private static final String REPORT_PATH = "target/jbehave/";
	
	private static final String TESTSUITE_TESTCASE_FAILURE = "count(//testcase/failure)";
	private static final String TESTSUITE_TESTCASE_IGNORED = "count(//testcase/skipped)";
	private static final String TESTSUITE_SKIPPED_ATTRIBUTE = "/testsuite/@skipped";
	private static final String TESTSUITE_TOTAL_TESTS_ATTRIBUTE = "/testsuite/@tests";
	private static final String TESTSUITE_NAME_ATTRIBUTE = "/testsuite/@name";
	private static final String TESTSUITE_FAILURE_ATTRIBUTE = "/testsuite/@failures";
	private static final String TESTSUITE_TESTCASE_COUNT = "count(//testcase)";

	private static XpathEngine eng = XMLUnit.newXpathEngine();

	@BeforeClass
	public static void createTestReports() {

        StoryReporterBuilder storyReporterBuilder = new StoryReporterBuilder()
                .withDefaultFormats()
                .withCodeLocation(CodeLocations.codeLocationFromClass(ReportTransformBehaviour.class))
                .withFormats(Format.XML);

        Configuration conf = new MostUsefulConfiguration()
                .useStoryReporterBuilder(storyReporterBuilder)
                .useFailureStrategy(new SilentlyAbsorbingFailure())
                .useStepCollector(new MarkUnmatchedStepsAsPending(new StepFinder(new ByLevenshteinDistance())));


		Embedder embedder = new Embedder() {

            @Override
			public List<CandidateSteps> candidateSteps() {
				return new InstanceStepsFactory(configuration(), new MySteps())
						.createCandidateSteps();
			}
		};

        embedder.useConfiguration(conf);
		
		embedder.useMetaFilters(Arrays.asList("-skip true"));

		ArrayList<String> storyPaths = new ArrayList<String>();
		storyPaths.add("all_successful.story");
		storyPaths.add("all_failed.story");
		storyPaths.add("examples_table_with_failure.story");
		storyPaths.add("given_story.story");
		storyPaths.add("given_failing_story.story");
		storyPaths.add("failure_followed_by_given_story.story");
		storyPaths.add("filter_scenario.story");
		storyPaths.add("filter_story.story");

		try {
			embedder.runStoriesAsPaths(storyPaths);
		} catch (Exception e) {
            e.printStackTrace();
		}
	}

	
	@Test
	public void transformSuccessfulStoryReport() throws Throwable {
		Document document = tranformReport(REPORT_PATH + "all_successful.xml");

		assertEquals("0", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTE, document));
		assertEquals("3", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document));
	}
	
	@Test
	public void transformStoryReportWithNoTitle() throws Throwable {
		Document document = tranformReport(REPORT_PATH + "all_successful.xml");	
		assertFalse("Test suite name is empty", eng.evaluate(TESTSUITE_NAME_ATTRIBUTE, document).isEmpty());
	}

	@Test
	public void transformFailedStoryReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "all_failed.xml");

		assertEquals("1", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document));
	}

	@Test
	public void transformStoryWithExamplesReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "examples_table_with_failure.xml");
		
		assertEquals("6", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTE, document));
		assertEquals("3", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document));
		assertEquals("6", eng.evaluate(TESTSUITE_TESTCASE_COUNT , document));
		assertEquals("3", eng.evaluate(TESTSUITE_TESTCASE_FAILURE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_IGNORED, document));
	}
	
	@Test
	public void transformGivenStoryReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "given_story.xml");

		assertEquals("0", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_COUNT, document));		
	}
	
	@Test
	public void transformFailedGivenStoryReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "given_failing_story.xml");

		assertEquals("1", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_COUNT, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_FAILURE, document));
	}
	
	@Test
	public void transformFailureFollowedByGivenStoryReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "failure_followed_by_given_story.xml");

		assertEquals("1", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTE, document));
		assertEquals("2", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document));
		assertEquals("2", eng.evaluate(TESTSUITE_TESTCASE_COUNT, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_FAILURE, document));
	}

	@Test
	public void transformFilterScenarioReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "filter_scenario.xml");

		assertEquals("1", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTE, document));
		assertEquals("2", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document));
		assertEquals("2", eng.evaluate(TESTSUITE_TESTCASE_COUNT, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_FAILURE, document));
	}
	
	@Test
	public void transformFilterStoryReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "filter_story.xml");

		assertEquals("0", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_TESTCASE_COUNT, document));
		assertEquals("0", eng.evaluate(TESTSUITE_TESTCASE_FAILURE, document));
	}
	
	private Document tranformReport(String reportFile)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException {

        // Might be running inside IDEA or Eclipse. Can't assume current directory.
        File cd = new File(this.getClass().getProtectionDomain()
                .getCodeSource().getLocation().getFile())
                .getParentFile().getParentFile();

        File file = new File(cd, reportFile);
        Source xmlSource = new StreamSource(file);
        File file1 = new File(cd, "src/main/resources/org/jbehave/hudson/jbehave-3.2-to-junit-1.0.xsl");
        Source xsltSource = new StreamSource(file1);

		Result resultOutput = new StreamResult(System.out);
		DOMResult result = new DOMResult();

		TransformerFactory transFact = TransformerFactory.newInstance();

		Transformer trans;
		trans = transFact.newTransformer(xsltSource);
		trans.transform(xmlSource, result);
		trans.transform(xmlSource, resultOutput);
		return (Document) result.getNode();
	}
	
}
