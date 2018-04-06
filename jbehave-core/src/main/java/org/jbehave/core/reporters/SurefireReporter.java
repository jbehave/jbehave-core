package org.jbehave.core.reporters;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.embedder.PerformableTree.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class SurefireReporter {

    private static final String SUREFIRE_FTL = "ftl/surefire.ftl";
    private final String reportName;
    private final Class<?> embeddableClass;
    private final boolean includeProperties;

    private TemplateProcessor processor = new FreemarkerProcessor();

	public SurefireReporter(String reportName, Class<?> embeddableClass, boolean includeProperties) {
        this.reportName = reportName;
        this.embeddableClass = embeddableClass;
        this.includeProperties = includeProperties;
    }

	public synchronized void generate(PerformableRoot root,
			File outputDirectory) {
        try {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("testsuite", new TestSuite(root.getStories(), embeddableClass, includeProperties));
            Writer writer = writer(outputDirectory, reportName);
            processor.process(SUREFIRE_FTL, dataModel, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate surefire report", e);
        }
    }

    private Writer writer(File outputDirectory, String name)
			throws IOException {
		File outputDir = new File(outputDirectory, "view");
		outputDir.mkdirs();
		return new FileWriter(new File(outputDir, name));
	}

	public static class TestSuite {

        private Class<?> embeddableClass;
        private TestCounts testCounts;
        private List<TestCase> testCases;
        private final boolean includeProperties;

        public TestSuite(List<PerformableStory> stories, Class<?> embeddableClass, boolean includeProperties) {
            this.embeddableClass = embeddableClass;
            testCounts = collectTestCounts(stories);
            testCases = collectTestCases(stories);
            this.includeProperties = includeProperties;
        }

        private TestCounts collectTestCounts(List<PerformableStory> stories) {
            TestCounts counts = new TestCounts();
            for (PerformableStory story : stories ){
                for ( PerformableScenario scenario : story.getScenarios() ){
                    Status status = scenario.getStatus();
                    if ( status == null ){
                        counts.addSkipped();
                        continue;
                    }
                    switch ( status ){
                        case FAILED:
                            counts.addFailure();
                            break;
                        case PENDING:
                        case NOT_ALLOWED:
                        case NOT_PERFORMED:
                            counts.addSkipped();
                            break;
                        case SUCCESSFUL:
                            counts.addSuccessful();
                            break;
                    }
                }
            }
            return counts;
        }

        private long totalTime(List<TestCase> testCases) {
            long total = 0;
            for ( TestCase tc : testCases ){
                total += tc.getTime();
            }
            return total;
        }

        private List<TestCase> collectTestCases(List<PerformableStory> stories) {
            List<TestCase> testCases = new ArrayList<>();
            int count = 1;
            for (PerformableStory story : stories ){
                for ( PerformableScenario scenario : story.getScenarios() ){
                    String title = scenario.getScenario().getTitle();
                    if ( title.equals(EMPTY) ){
                        title="No title "+count++;
                    }
                    long time = scenario.getTiming().getDurationInMillis();
                    TestCase tc = new TestCase(title, embeddableClass, time);
                    if ( scenario.getStatus() == Status.FAILED ){
                        tc.setFailure(new TestFailure(scenario.getFailure()));
                    }
                    testCases.add(tc);
                }
            }
            return testCases;
        }

        public String getName(){
            return embeddableClass.getName();
        }

        public long getTime(){
            return totalTime(testCases);
        }

        public int getTests(){
            return testCounts.getTests();
        }

        public int getSkipped(){
            return testCounts.getSkipped();
        }

        public int getErrors(){
            return testCounts.getErrors();
        }

        public int getFailures(){
            return testCounts.getFailures();
        }

        public Properties getProperties(){
            return includeProperties ? System.getProperties() : new Properties();
        }

        public List<TestCase> getTestCases(){
            return testCases;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }

    public static class TestCase {
	    private final String name;
        private final Class<?> embeddableClass;
        private long time;
        private TestFailure failure;

        public TestCase(String name, Class<?> embeddableClass, long time) {
            this.name = name;
            this.embeddableClass = embeddableClass;
            this.time = time;
        }

        public String getName(){
            return name;
        }

        public String getClassname(){
            return embeddableClass.getName();
        }

        public long getTime(){
            return time;
        }

        public boolean hasFailure(){
            return failure != null;
        }

        public TestFailure getFailure(){
            return failure;
        }

        public void setFailure(TestFailure failure) {
            this.failure = failure;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }

    }

    public static class TestFailure {

        private final Throwable failure;

        public TestFailure(Throwable failure) {
            this.failure = failure;
        }

        public boolean hasFailure(){
            return failure != null;
        }

        public String getMessage(){
            if ( hasFailure() ) {
                return failure.getMessage();
            }
            return EMPTY;
        }

        public String getType(){
            if ( hasFailure() ) {
                return failure.getClass().getName();
            }
            return EMPTY;
        }

        public String getStackTrace(){
            if ( hasFailure() ){
                return new StackTraceFormatter(true).stackTrace(failure);
            }
            return EMPTY;
        }
    }

    public static class TestCounts {

        private int tests = 0;
        private int skipped = 0;
        private int errors = 0;
        private int failures = 0;

        public int getTests() {
            return tests;
        }

        public int getSkipped(){
            return skipped;
        }

        public int getErrors(){
            return errors;
        }

        public int getFailures(){
            return failures;
        }

        public void addFailure() {
            failures++;
            tests++;
        }

        public void addSkipped() {
            skipped++;
            tests++;
        }

        public void addSuccessful() {
            tests++;
        }
    }
}
