package org.jbehave.hudson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepFinder.ByLevenshteinDistance;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ReportTransformBehaviour {
    private static final String TESTCASE_COUNT = "count(//testcase)";
    private static final String TESTCASE_FAILURE = "count(//testcase/failure)";
    private static final String TESTCASE_IGNORED = "count(//testcase/skipped)";
    private static final String TESTSUITE_SKIPPED_ATTRIBUTE = "/testsuite/@skipped";
    private static final String TESTSUITE_TESTS_ATTRIBUTE = "/testsuite/@tests";
    private static final String TESTSUITE_NAME_ATTRIBUTE = "/testsuite/@name";
    private static final String TESTSUITE_FAILURES_ATTRIBUTE = "/testsuite/@failures";

    private static XpathEngine engine = XMLUnit.newXpathEngine();
    // Might be running inside IDEA or Eclipse. Can't assume current directory.
    private File cd = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile())
            .getParentFile().getParentFile();

    @Test
    public void transformSuccessfulStoryReport() throws Throwable {
        runStories("all_successful.story");
        Document document = tranformReport("all_successful.xml");
        assertThat(engine.evaluate(TESTSUITE_FAILURES_ATTRIBUTE, document), equalTo("0"));
        assertThat(engine.evaluate(TESTSUITE_TESTS_ATTRIBUTE, document), equalTo("3"));
        assertThat(engine.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document), equalTo("0"));
    }

    @Test
    public void transformStoryReportWithNoTitle() throws Throwable {
        runStories("all_successful.story");
        Document document = tranformReport("all_successful.xml");
        assertThat(engine.evaluate(TESTSUITE_NAME_ATTRIBUTE, document).length(), is(not(0)));
    }

    @Test
    public void transformFailedStoryReport() throws FileNotFoundException, Throwable {
        runStories("all_failed.story");
        Document document = tranformReport("all_failed.xml");

        assertThat(engine.evaluate(TESTSUITE_FAILURES_ATTRIBUTE, document), equalTo("1"));
        assertThat(engine.evaluate(TESTSUITE_TESTS_ATTRIBUTE, document), equalTo("1"));
        assertThat(engine.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document), equalTo("0"));
    }

    @Test
    public void transformStoryWithExamplesReport() throws FileNotFoundException, Throwable {
        runStories("examples_table_with_failure.story");
        Document document = tranformReport("examples_table_with_failure.xml");

        assertThat(engine.evaluate(TESTSUITE_TESTS_ATTRIBUTE, document), equalTo("6"));
        assertThat(engine.evaluate(TESTSUITE_FAILURES_ATTRIBUTE, document), equalTo("3"));
        assertThat(engine.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document), equalTo("1"));
        assertThat(engine.evaluate(TESTCASE_COUNT, document), equalTo("6"));
        assertThat(engine.evaluate(TESTCASE_FAILURE, document), equalTo("3"));
        assertThat(engine.evaluate(TESTCASE_IGNORED, document), equalTo("1"));
    }

    @Test
    public void transformGivenStoryReport() throws FileNotFoundException, Throwable {
        runStories("given_story.story");
        Document document = tranformReport("given_story.xml");

        assertThat(engine.evaluate(TESTSUITE_FAILURES_ATTRIBUTE, document), equalTo("0"));
        assertThat(engine.evaluate(TESTSUITE_TESTS_ATTRIBUTE, document), equalTo("1"));
        assertThat(engine.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document), equalTo("0"));
        assertThat(engine.evaluate(TESTCASE_COUNT, document), equalTo("1"));
    }

    @Test
    public void transformFailedGivenStoryReport() throws FileNotFoundException, Throwable {
        runStories("given_failing_story.story");
        Document document = tranformReport("given_failing_story.xml");

        assertThat(engine.evaluate(TESTSUITE_FAILURES_ATTRIBUTE, document), equalTo("1"));
        assertThat(engine.evaluate(TESTSUITE_TESTS_ATTRIBUTE, document), equalTo("1"));
        assertThat(engine.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document), equalTo("0"));
        assertThat(engine.evaluate(TESTCASE_COUNT, document), equalTo("1"));
        assertThat(engine.evaluate(TESTCASE_FAILURE, document), equalTo("1"));
    }

    @Test
    public void transformFailureFollowedByGivenStoryReport() throws FileNotFoundException, Throwable {
        runStories("failure_followed_by_given_story.story");
        Document document = tranformReport("failure_followed_by_given_story.xml");

        assertThat(engine.evaluate(TESTSUITE_FAILURES_ATTRIBUTE, document), equalTo("1"));
        assertThat(engine.evaluate(TESTSUITE_TESTS_ATTRIBUTE, document), equalTo("2"));
        assertThat(engine.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document), equalTo("0"));
        assertThat(engine.evaluate(TESTCASE_COUNT, document), equalTo("2"));
        assertThat(engine.evaluate(TESTCASE_FAILURE, document), equalTo("1"));
    }

    @Test
    @Ignore("Filtered stories not run anymore")
    public void transformFilterScenarioReport() throws FileNotFoundException, Throwable {
        runStories("filter_scenario.story");
        Document document = tranformReport("filter_scenario.xml");

        assertThat(engine.evaluate(TESTSUITE_FAILURES_ATTRIBUTE, document), equalTo("1"));
        assertThat(engine.evaluate(TESTSUITE_TESTS_ATTRIBUTE, document), equalTo("2"));
        assertThat(engine.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document), equalTo("0"));
        assertThat(engine.evaluate(TESTCASE_COUNT, document), equalTo("2"));
        assertThat(engine.evaluate(TESTCASE_FAILURE, document), equalTo("1"));
    }

    @Test
    @Ignore("Filtered stories not run anymore")
    public void transformFilterStoryReport() throws FileNotFoundException, Throwable {
        runStories("filter_story.story");
        Document document = tranformReport("filter_story.xml");

        assertThat(engine.evaluate(TESTSUITE_FAILURES_ATTRIBUTE, document), equalTo("0"));
        assertThat(engine.evaluate(TESTSUITE_TESTS_ATTRIBUTE, document), equalTo("0"));
        assertThat(engine.evaluate(TESTSUITE_SKIPPED_ATTRIBUTE, document), equalTo("0"));
        assertThat(engine.evaluate(TESTCASE_COUNT, document), equalTo("0"));
        assertThat(engine.evaluate(TESTCASE_FAILURE, document), equalTo("0"));
    }

    @Test
    public void testsuiteNameTitle() throws FileNotFoundException, Throwable {
        runStories("title.story");
        Document document = tranformReport("title.xml");
        assertThat(engine.evaluate(TESTSUITE_NAME_ATTRIBUTE, document), equalTo("title.story"));
    }

    private void runStories(String... storyPaths) {
        StoryReporterBuilder storyReporterBuilder = new StoryReporterBuilder().withDefaultFormats()
                .withCodeLocation(CodeLocations.codeLocationFromClass(ReportTransformBehaviour.class))
                .withFormats(Format.XML);

        Configuration configuration = new MostUsefulConfiguration()
                .useStoryLoader(new LoadFromClasspath(this.getClass())).useStoryReporterBuilder(storyReporterBuilder)
                .useFailureStrategy(new SilentlyAbsorbingFailure())
                .useStepCollector(new MarkUnmatchedStepsAsPending(new StepFinder(new ByLevenshteinDistance())));

        Embedder embedder = new Embedder();
        embedder.useEmbedderControls(new EmbedderControls().doGenerateViewAfterStories(false));
        embedder.useConfiguration(configuration);
        embedder.useCandidateSteps(new InstanceStepsFactory(configuration, new MySteps()).createCandidateSteps());
        embedder.useMetaFilters(asList("-skip true"));

        try {
            embedder.runStoriesAsPaths(asList(storyPaths));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document tranformReport(String path) throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException {

        File report = new File(cd, "/target/jbehave/" + path);
        try {
			String out = FileUtils.readFileToString(report);
			System.out.println(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Source xml = new StreamSource(report);
        Source xslt = new StreamSource(new File(cd,
                "src/main/resources/org/jbehave/hudson/"+new JBehaveInputMetric().getXslName()));

        Result resultOutput = new StreamResult(System.out);
        DOMResult result = new DOMResult();

        Transformer transformer = TransformerFactory.newInstance().newTransformer(xslt);
        transformer.transform(xml, result);
        transformer.transform(xml, resultOutput);
        return (Document) result.getNode();
    }

}
