package org.jbehave.hudson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.StoryReporterBuilder.Format;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepFinder.ByLevenshteinDistance;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class ReportTransformTest {
	private static final String REPORT_PATH = "target/jbehave/";
	
	private static final String TESTSUITE_TESTCASE_FAILURE = "count(//testcase/failure)";
	private static final String TESTSUITE_TESTCASE_IGNORED = "count(//testcase/skipped)";
	private static final String TESTSUITE_SKIPPED_ATTRIBUTTE = "/testsuite/@skipped";
	private static final String TESTSUITE_TOTAL_TESTS_ATTRIBUTTE = "/testsuite/@tests";
	private static final String TESTSUITE_NAME_ATTRIBUTTE = "/testsuite/@name";
	private static final String TESTSUITE_FAILURE_ATTRIBUTTE = "/testsuite/@failures";
	private static final String TESTSUITE_TESTCASE_COUNT = "count(//testcase)";

	private static XpathEngine eng = XMLUnit.newXpathEngine();

	@BeforeClass
	public static void createTestReports() {

		Embedder embedder = new Embedder() {
			@Override
			public Configuration configuration() {

				Configuration configuration = new MostUsefulConfiguration()
						.useStoryReporterBuilder(
								new StoryReporterBuilder().withDefaultFormats()
										.withFormats(Format.XML))
						.useFailureStrategy(new SilentlyAbsorbingFailure())
						.useStepCollector(new MarkUnmatchedStepsAsPending(new StepFinder(new ByLevenshteinDistance())));

				return configuration;

			}

			@Override
			public List<CandidateSteps> candidateSteps() {
				return new InstanceStepsFactory(configuration(), new MySteps())
						.createCandidateSteps();
			}
		};

		ArrayList<String> storyPaths = new ArrayList<String>();
		storyPaths.add("all_successful.story");
		storyPaths.add("all_failed.story");
		storyPaths.add("examples_table_with_failure.story");
		storyPaths.add("given_story.story");
		storyPaths.add("given_failing_story.story");
		storyPaths.add("failure_followed_by_given_story.story");

		try {
			embedder.runStoriesAsPaths(storyPaths);
		} catch (Exception e) {

		}
	};

	
	@Test
	public void transformSuccessfulStoryReport() throws Throwable {
		Document document = tranformReport(REPORT_PATH + "all_successful.xml");

		assertEquals("0", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTTE, document));
	}
	
	@Test
	public void transformStoryReportWithNoTitle() throws Throwable {
		Document document = tranformReport(REPORT_PATH + "all_successful.xml");	
		assertFalse("Test suite name is empty", eng.evaluate(TESTSUITE_NAME_ATTRIBUTTE, document).isEmpty());
	}

	@Test
	public void transformFailedStoryReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "all_failed.xml");

		assertEquals("1", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTTE, document));
	}

	@Test
	public void transformStoryWithExamplesReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "examples_table_with_failure.xml");
		
		assertEquals("6", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTTE, document));
		assertEquals("3", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTTE, document));
		assertEquals("6", eng.evaluate(TESTSUITE_TESTCASE_COUNT , document));
		assertEquals("3", eng.evaluate(TESTSUITE_TESTCASE_FAILURE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_IGNORED, document));
	}
	
	@Test
	public void transformGivenStoryReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "given_story.xml");

		assertEquals("0", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_COUNT, document));		
	}
	
	@Test
	public void transformFailedGivenStoryReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "given_failing_story.xml");

		assertEquals("1", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTTE, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_COUNT, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_FAILURE, document));
	}
	
	@Test
	public void transformFailureFollowedByGivenStoryReport() throws FileNotFoundException, Throwable {
		Document document = tranformReport(REPORT_PATH + "failure_followed_by_given_story.xml");

		assertEquals("1", eng.evaluate(TESTSUITE_FAILURE_ATTRIBUTTE, document));
		assertEquals("2", eng.evaluate(TESTSUITE_TOTAL_TESTS_ATTRIBUTTE, document));
		assertEquals("0", eng.evaluate(TESTSUITE_SKIPPED_ATTRIBUTTE, document));
		assertEquals("2", eng.evaluate(TESTSUITE_TESTCASE_COUNT, document));
		assertEquals("1", eng.evaluate(TESTSUITE_TESTCASE_FAILURE, document));
	}

	private Document tranformReport(String reportFile)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException {

		Source xmlSource = new StreamSource(new File(reportFile));
		Source xsltSource = new StreamSource(new File(
				"src/main/resources/org/jbehave/hudson/jbehave-3.2-to-junit-1.0.xsl"));

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
