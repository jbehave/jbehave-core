package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.embedder.PerformableTree.PerformableRoot;
import org.jbehave.core.embedder.PerformableTree.PerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.embedder.PerformableTree.Status;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.xml.sax.SAXException;

public class SurefireReporter {

    private static final String SUREFIRE_FTL = "ftl/surefire-xml-report.ftl";
    private static final String SUREFIRE_XSD = "xsd/surefire-test-report.xsd";
    private static final String XML = ".xml";
    private static final String DOT = ".";
    private static final String HYPHEN = "-";
    private static final String SLASH = "/";

    private final Class<?> embeddableClass;
    private final TestCaseNamingStrategy namingStrategy;
    private final boolean includeProperties;
    private final String reportName;
    private final boolean reportByStory;

    private TemplateProcessor processor = new FreemarkerProcessor();

    public static class Options {
        public static final String DEFAULT_REPORT_NAME = "jbehave-surefire";
        public static final TestCaseNamingStrategy DEFAULT_NAMING_STRATEGY = new SimpleNamingStrategy();
        public static final boolean DEFAULT_INCLUDE_PROPERTIES = true;
        public static final boolean DEFAULT_REPORT_BY_STORY = false;

        private String reportName;
        private TestCaseNamingStrategy namingStrategy;
        private boolean includeProperties;
        private boolean reportByStory;

        public Options() {
            this(DEFAULT_REPORT_NAME, DEFAULT_NAMING_STRATEGY, DEFAULT_REPORT_BY_STORY, DEFAULT_INCLUDE_PROPERTIES);
        }

        public Options(String reportName, TestCaseNamingStrategy namingStrategy, boolean reportByStory, boolean includeProperties) {
            this.reportName = reportName;
            this.namingStrategy = namingStrategy;
            this.includeProperties = includeProperties;
            this.reportByStory = reportByStory;
        }

        public Options useReportName(String reportName) {
            this.reportName = reportName;
            return this;
        }

        public Options withNamingStrategy(TestCaseNamingStrategy strategy) {
            this.namingStrategy = strategy;
            return this;
        }

        public Options doReportByStory(boolean reportByStory) {
            this.reportByStory = reportByStory;
            return this;
        }

        public Options doIncludeProperties(boolean includeProperties) {
            this.includeProperties = includeProperties;
            return this;
        }

    }

    public SurefireReporter(Class<?> embeddableClass) {
        this(embeddableClass, new Options());
    }

    public SurefireReporter(Class<?> embeddableClass, Options options) {
        this.embeddableClass = embeddableClass;
        this.namingStrategy = options.namingStrategy;
        this.includeProperties = options.includeProperties;
        this.reportName = options.reportName;
        this.reportByStory = options.reportByStory;
    }

    public synchronized void generate(PerformableRoot root,
                                      File outputDirectory) {
        List<PerformableStory> stories = root.getStories();
        if (reportByStory) {
            for (PerformableStory story : stories) {
                String name = reportName(story.getStory().getPath());
                File file = outputFile(outputDirectory, name);
                generateReport(asList(story), file);
            }
        } else {
            File file = outputFile(outputDirectory, reportName);
            generateReport(stories, file);
        }
    }

    private String reportName(String path) {
        return reportName + HYPHEN + StringUtils.replaceAll(StringUtils.substringBefore(path, DOT), SLASH, DOT);
    }

    private void generateReport(List<PerformableStory> stories, File file) {
        try {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("testsuite", new TestSuite(embeddableClass, namingStrategy, stories, includeProperties));
            processor.process(SUREFIRE_FTL, dataModel, new FileWriter(file));
            validateOutput(file, SUREFIRE_XSD);
        } catch (IOException | SAXException e) {
            throw new RuntimeException("Failed to generate surefire report", e);
        }
    }

    private File outputFile(File outputDirectory, String name) {
        File outputDir = new File(outputDirectory, "view");
        outputDir.mkdirs();
        if (!name.endsWith(XML)) {
            name = name + XML;
        }
        return new File(outputDir, name);
    }

    private void validateOutput(File file, String surefireXsd) throws SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new StreamSource(this.getClass().getClassLoader().getResourceAsStream(surefireXsd)));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(file));
    }

    public static class TestSuite {

        private final Class<?> embeddableClass;
        private final TestCaseNamingStrategy namingStrategy;
        private final TestCounts testCounts;
        private final List<TestCase> testCases;
        private final boolean includeProperties;

        public TestSuite(Class<?> embeddableClass, TestCaseNamingStrategy namingStrategy, List<PerformableStory> stories, boolean includeProperties) {
            this.embeddableClass = embeddableClass;
            this.namingStrategy = namingStrategy;
            this.testCounts = collectTestCounts(stories);
            this.testCases = collectTestCases(stories);
            this.includeProperties = includeProperties;
        }

        private TestCounts collectTestCounts(List<PerformableStory> stories) {
            TestCounts counts = new TestCounts();
            for (PerformableStory story : stories) {
                for (PerformableScenario scenario : story.getScenarios()) {
                    Status status = scenario.getStatus();
                    if (status == null) {
                        counts.addSkipped();
                        continue;
                    }
                    switch (status) {
                        case FAILED:
                            counts.addFailure();
                            break;
                        case PENDING:
                        case EXCLUDED:
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
            for (TestCase tc : testCases) {
                total += tc.getTime();
            }
            return total;
        }

        private List<TestCase> collectTestCases(List<PerformableStory> stories) {
            List<TestCase> testCases = new ArrayList<>();
            for (PerformableStory story : stories) {
                for (PerformableScenario scenario : story.getScenarios()) {
                    String name = namingStrategy.resolveName(story.getStory(), scenario.getScenario());
                    long time = scenario.getTiming().getDurationInMillis();
                    TestCase tc = new TestCase(embeddableClass, name, time);
                    if (scenario.getStatus() == Status.FAILED) {
                        tc.setFailure(new TestFailure(scenario.getFailure()));
                    }
                    testCases.add(tc);
                }
            }
            return testCases;
        }

        public String getName() {
            return embeddableClass.getName();
        }

        public long getTime() {
            return totalTime(testCases);
        }

        public int getTests() {
            return testCounts.getTests();
        }

        public int getSkipped() {
            return testCounts.getSkipped();
        }

        public int getErrors() {
            return testCounts.getErrors();
        }

        public int getFailures() {
            return testCounts.getFailures();
        }

        public Properties getProperties() {
            return includeProperties ? System.getProperties() : new Properties();
        }

        public List<TestCase> getTestCases() {
            return testCases;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }

    public static class TestCase {
        private final Class<?> embeddableClass;
        private final String name;
        private long time;
        private TestFailure failure;

        public TestCase(Class<?> embeddableClass, String name, long time) {
            this.embeddableClass = embeddableClass;
            this.name = name;
            this.time = time;
        }

        public String getName() {
            return name;
        }

        public String getClassname() {
            return embeddableClass.getName();
        }

        public long getTime() {
            return time;
        }

        public boolean hasFailure() {
            return failure != null;
        }

        public TestFailure getFailure() {
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

    public interface TestCaseNamingStrategy {

        String resolveName(Story story, Scenario scenario);

    }

    /**
     * A simple naming strategy:  [story name].[scenario title]
     */
    public static class SimpleNamingStrategy implements TestCaseNamingStrategy {

        @Override
        public String resolveName(Story story, Scenario scenario) {
            String path = story.getPath();
            File file = new File(path);
            String name = StringUtils.substringBefore(file.getName(), DOT);
            return name + DOT + scenario.getTitle();
        }

    }

    /**
     * A breadcrumb-based naming strategy:  [story path with breadcrumbs].[story name].[scenario title]
     */
    public static class BreadcrumbNamingStrategy implements TestCaseNamingStrategy {

        private static final String DEFAULT_BREADCRUMB = " > ";

        private final String breadcrumb;

        public BreadcrumbNamingStrategy() {
            this(DEFAULT_BREADCRUMB);
        }

        public BreadcrumbNamingStrategy(String breadcrumb) {
            this.breadcrumb = breadcrumb;
        }

        @Override
        public String resolveName(Story story, Scenario scenario) {
            String path = story.getPath();
            File file = new File(path);
            List<String> parentNames = new ArrayList<>();
            collectParentNames(file, parentNames);
            String parentPath = StringUtils.join(parentNames, breadcrumb);
            String name = StringUtils.substringBefore(file.getName(), DOT);
            return parentPath + breadcrumb + name + DOT + scenario.getTitle();
        }

        private void collectParentNames(File file, List<String> parents) {
            if (file.getParent() != null) {
                String name = file.getParentFile().getName();
                if (!StringUtils.isBlank(name)) {
                    parents.add(0, name);
                }
                collectParentNames(file.getParentFile(), parents);
            }
        }


    }

    public static class TestFailure {

        private final Throwable failure;

        public TestFailure(Throwable failure) {
            this.failure = failure;
        }

        public boolean hasFailure() {
            return failure != null;
        }

        public String getMessage() {
            if (hasFailure()) {
                return EscapeMode.XML.escapeString(failure.getMessage());
            }
            return EMPTY;
        }

        public String getType() {
            if (hasFailure()) {
                return failure.getClass().getName();
            }
            return EMPTY;
        }

        public String getStackTrace() {
            if (hasFailure()) {
                String stackTrace = new StackTraceFormatter(true).stackTrace(failure);
                return EscapeMode.XML.escapeString(stackTrace);
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

        public int getSkipped() {
            return skipped;
        }

        public int getErrors() {
            return errors;
        }

        public int getFailures() {
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
