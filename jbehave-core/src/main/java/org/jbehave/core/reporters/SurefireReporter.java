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

    private TemplateProcessor processor = new FreemarkerProcessor();

	public SurefireReporter(String reportName, Class<?> embeddableClass) {
        this.reportName = reportName;
        this.embeddableClass = embeddableClass;
    }

	public synchronized void generate(PerformableRoot root,
			File outputDirectory) {
        try {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("testsuite", new TestSuite(root, embeddableClass));
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
        private String name;
        private Map<String, Long> times;
        private TestCounts counts;
        private long time;

        public TestSuite(PerformableRoot root, Class<?> embeddableClass) {
            this.embeddableClass = embeddableClass;
            counts = countTests(root);
            times = collectTimes(root);
            name = embeddableClass.getName();
            time = total(times);
        }

        private TestCounts countTests(PerformableRoot root) {
            TestCounts counts = new TestCounts();
            for (PerformableStory story : root.getStories() ){
                for ( PerformableScenario scenario : story.getScenarios() ){
                    Status status = scenario.getStatus();
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

        private long total(Map<String, Long> times) {
            long total = 0;
            for ( long value : times.values() ){
                total += value;
            }
            return total;
        }

        private Map<String, Long> collectTimes(PerformableRoot root) {
            Map<String,Long> times = new HashMap<>();
            int count = 0;
            for (PerformableStory story : root.getStories() ){
                for ( PerformableScenario scenario : story.getScenarios() ){
                    String title = scenario.getScenario().getTitle();
                    if ( title.equals(EMPTY) ){
                        title="No title "+count++;
                    }
                    times.put(title, scenario.getTiming().getDurationInMillis());
                }
            }
            return times;
        }

        public String getName(){
            return name;
        }

        public long getTime(){
            return time;
        }

        public int getTests(){
            return counts.getTests();
        }

        public int getSkipped(){
            return counts.getSkipped();
        }

        public int getErrors(){
            return counts.getErrors();
        }

        public int getFailures(){
            return counts.getFailures();
        }

        public Properties getProperties(){
            return System.getProperties();
        }

        public List<TestCase> getTestCases(){
            List<TestCase> list = new ArrayList<>();
            for ( String name : times.keySet() ){
                list.add(new TestCase(name, embeddableClass, times.get(name)));
            }
            return list;
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

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
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
