package org.jbehave.core.reporters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Type;
import org.jbehave.core.failures.KnownFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.*;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryNarrator.IsDateEqual;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.Timing;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;

class PrintStreamOutputBehaviour extends AbstractOutputBehaviour {

    @Test
    void shouldOutputStoryToTxt() throws IOException {
        // Given
        String name = "stream-story.txt";
        File file = newFile("target/" +name);
        StoryReporter reporter = new TxtOutput(createPrintStream(file));

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        assertFileOutputIsSameAs(file, name);
    }


    @Test
    void shouldOutputStoryToTxtWhenExcludedByFilter() throws IOException {
        // Given
        String name = "stream-story-excluded.txt";
        File file = newFile("target/"+ name);
        StoryReporter reporter = new TxtOutput(createPrintStream(file));

        // When
        StoryNarrator
                .narrateAnInterestingStoryExcludedByFilter(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    void shouldOutputStoryToTxtUsingCustomPatterns() throws IOException {
        // Given
        String name = "stream-story-custom-patterns.txt";
        File file = newFile("target/"+ name);
        Properties patterns = new Properties();
        patterns.setProperty("pending", "{0} - {1} - need to implement me\n");
        patterns.setProperty("failed", "{0} <<< {1}\n");
        patterns.setProperty("notPerformed", "{0} : {1} (because of previous pending)\n");
        StoryReporter reporter = new TxtOutput(createPrintStream(file), patterns);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, true);

        // Then
        assertFileOutputIsSameAs(file, name);

    }

    @Test
    void shouldOutputStoryToHtml() throws IOException {
        // Given
        String name = "stream-story.html";
        File file = newFile("target/" + name);
        StoryReporter reporter = new HtmlOutput(createPrintStream(file));

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    void shouldOutputStoryToHtmlWhenExcludedByFilter() throws IOException {
        // Given
        String name = "stream-story-excluded.html";
        File file = newFile("target/" + name);
        StoryReporter reporter = new HtmlOutput(createPrintStream(file));

        // When
        StoryNarrator
                .narrateAnInterestingStoryExcludedByFilter(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    void shouldOutputStoryToHtmlUsingCustomPatterns() throws IOException {
        // Given
        String name = "stream-story-custom-patterns.html";
        File file = newFile("target/" + name);
        Properties patterns = new Properties();
        patterns.setProperty("afterStory", "</div><!-- after story -->\n");
        patterns.setProperty("afterScenario", "</div><!-- after scenario -->\n");
        patterns.setProperty("afterExamples", "</div><!-- after examples -->\n");
        StoryReporter reporter = new HtmlOutput(createPrintStream(file), patterns);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    void shouldOutputStoryToXml() throws IOException, SAXException {
        // Given
        String name = "stream-story.xml";
        File file = newFile("target/" + name);
        StoryReporter reporter = new XmlOutput(createPrintStream(file));

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
        validateFileOutput(file);
    }

    @Test
    void shouldOutputStoryToXmlWhenExcludedByFilter() throws IOException {
        // Given
        String name = "stream-story-excluded.xml";
        File file = newFile("target/" + name);
        StoryReporter reporter = new XmlOutput(createPrintStream(file));

        // When
        StoryNarrator
                .narrateAnInterestingStoryExcludedByFilter(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    void shouldOutputStoryToXmlUsingCustomPatterns() throws IOException {
        // Given
        String name = "stream-story-custom-patterns.xml";
        File file = newFile("target/" + name);
        Properties patterns = new Properties();
        patterns.setProperty("afterStory", "</story><!-- after story -->\n");
        patterns.setProperty("afterScenario", "</scenario><!-- after scenario -->\n");
        patterns.setProperty("afterExamples", "</examples><!-- after examples -->\n");
        StoryReporter reporter = new XmlOutput(createPrintStream(file), patterns);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    void shouldOutputStoryJson() throws IOException, SAXException {
        // Given
        String name = "stream-story.json";
        File file = new File("target/" + name);
        StoryReporter reporter = new JsonOutput(new FilePrintStreamFactory.FilePrintStream(file, false),
                new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        assertJson(name, fileContent(file));
    }

    @Test
    void shouldReportEventsToJsonOutputScenarioNestedGivenStoriesWithMultipleExamplesAndLifecycles() throws IOException
    {
        ExamplesTable examplesTable = new ExamplesTable("|key|row|\n|key1|row1|\n|key2|row2|");
        Lifecycle lifecycle = new Lifecycle(new ExamplesTable("|key|row|\n|key1|row1|\n|key2|row2|"));
        Map<String, String> example = new HashMap<>();
        example.put("key1", "value1");
        example.put("key2", "value2");

        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new JsonOutput(new PrintStream(out), new Properties(), new LocalizedKeywords());

        // When
        Story givenStory = spyStoryUuid(new Story("/path/to/story", new Description("Given story"),
                new Narrative("renovate my house", "customer", "get a loan"), new ArrayList<Scenario>()));
        Story rootStory = spyStoryUuid(new Story("/path/to/story", new Description("Root story"),
                new Narrative("renovate my house", "customer", "get a loan"), new ArrayList<Scenario>()));

        String step = "My step";
        Scenario scenario = spyScenarioUuid(new Scenario("My scenario", Meta.EMPTY, null, examplesTable, Collections.<String>emptyList()));
        Timing timing = getTiming();

        reporter.beforeStory(rootStory, false);
        reporter.lifecyle(lifecycle);
        reporter.beforeScenarios();
        reporter.beforeScenario(scenario);
        reporter.beforeExamples(Collections.singletonList(step), examplesTable);
        reporter.example(example, 0);
        reportStep(reporter, step, Stage.BEFORE);
        reporter.beforeGivenStories();
        reporter.givenStories(Collections.singletonList(givenStory.getPath()));
        reporter.beforeStory(givenStory, true);
        reporter.lifecyle(lifecycle);
        reporter.beforeScenarios();
        reporter.beforeScenario(scenario);
        reporter.beforeExamples(Collections.singletonList(step), examplesTable);
        reporter.example(example, 0);
        reportStep(reporter, step, Stage.BEFORE);
        reporter.beforeGivenStories();
        reporter.givenStories(Collections.singletonList(givenStory.getPath()));
        reporter.beforeStory(givenStory, true);
        reporter.lifecyle(lifecycle);
        reporter.beforeScenarios();
        reporter.beforeScenario(scenario);
        reporter.beforeExamples(Collections.singletonList(step), examplesTable);
        reporter.example(example, 0);
        reportStep(reporter, step, Stage.BEFORE);
        reportStep(reporter, step, null);
        reportStep(reporter, step, Stage.AFTER);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.afterScenarios();
        reporter.afterStory(true);
        reporter.afterGivenStories();
        reportStep(reporter, step, null);
        reportStep(reporter, step, Stage.AFTER);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.afterScenarios();
        reporter.afterStory(true);
        reporter.afterGivenStories();
        reportStep(reporter, step, null);
        reportStep(reporter, step, Stage.AFTER);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.beforeScenario(scenario);
        reporter.beforeExamples(Collections.singletonList(step), examplesTable);
        reporter.example(example, 0);
        reportStep(reporter, step, Stage.BEFORE);
        reporter.beforeGivenStories();
        reporter.givenStories(Collections.singletonList(givenStory.getPath()));
        reporter.beforeStory(givenStory, true);
        reporter.lifecyle(lifecycle);
        reporter.beforeScenarios();
        reporter.beforeScenario(scenario);
        reporter.beforeExamples(Collections.singletonList(step), examplesTable);
        reporter.example(example, 0);
        reportStep(reporter, step, Stage.BEFORE);
        reporter.beforeGivenStories();
        reporter.givenStories(Collections.singletonList(givenStory.getPath()));
        reporter.beforeStory(givenStory, true);
        reporter.lifecyle(lifecycle);
        reporter.beforeScenarios();
        reporter.beforeScenario(scenario);
        reporter.beforeExamples(Collections.singletonList(step), examplesTable);
        reporter.example(example, 0);
        reportStep(reporter, step, Stage.BEFORE);
        reportStep(reporter, step, null);
        reportStep(reporter, step, Stage.AFTER);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.afterScenarios();
        reporter.afterStory(true);
        reporter.afterGivenStories();
        reportStep(reporter, step, null);
        reportStep(reporter, step, Stage.AFTER);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.afterScenarios();
        reporter.afterStory(true);
        reporter.afterGivenStories();
        reportStep(reporter, step, null);
        reportStep(reporter, step, Stage.AFTER);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.afterScenarios();
        reporter.afterStory(false);

        // Then
        assertJson("story-level-examples.json", out.toString());
    }

    @Test
    void shouldReportEventsToJsonOutputIfScenarioIsEmptyWithLifecycleExamplesTable() throws IOException
    {
        ExamplesTable examplesTable = new ExamplesTable("|key|row|\n|key1|row1|\n|key2|row2|");
        Lifecycle lifecycle = new Lifecycle(new ExamplesTable("|key|row|\n|key1|row1|\n|key2|row2|"));
        Map<String, String> example = new HashMap<>();

        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new JsonOutput(new PrintStream(out), new Properties(), new LocalizedKeywords());

        // When
        Story rootStory = spyStoryUuid(new Story("/path/to/story", new Description("Root story"),
                new Narrative("renovate my house", "customer", "get a loan"), new ArrayList<Scenario>()));

        Scenario scenario = spyScenarioUuid(new Scenario("My scenario", Meta.EMPTY, null, examplesTable,
                Collections.<String>emptyList()));
        Timing timing = getTiming();

        reporter.beforeStory(rootStory, false);
        reporter.lifecyle(lifecycle);
        reporter.beforeScenarios();
        reporter.beforeScenario(scenario);
        reporter.beforeExamples(Collections.emptyList(), examplesTable);
        reporter.example(example, 0);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.beforeScenario(scenario);
        reporter.beforeExamples(Collections.emptyList(), examplesTable);
        reporter.example(example, 0);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.afterScenarios();
        reporter.afterStory(false);

        // Then
        String expected = "{\"id\": \"story-id\", \"path\": \"\\/path\\/to\\/story\", \"title\": \"Root story\",\"lifecycle\":"
                + " {\"keyword\": \"Lifecycle:\",\"parameters\": {\"names\": [\"key\",\"row\"],\"values\": [[\"key1\",\"row1\"]"
                + ",[\"key2\",\"row2\"]]}},\"scenarios\": [{\"keyword\": \"Scenario:\", \"id\": \"scenario-id\", \"title\": "
                + "\"My scenario\",\"examples\": {\"keyword\": \"Examples:\",\"steps\": [],\"parameters\": {\"names\": "
                + "[\"key\",\"row\"],\"values\": [[\"key1\",\"row1\"],[\"key2\",\"row2\"]]},\"examples\": [{\"keyword\": "
                + "\"Example:\", \"parameters\": {}}]},\"start\":1,\"end\":2},{\"keyword\": \"Scenario:\", \"id\": "
                + "\"scenario-id\", \"title\": \"My scenario\",\"examples\": {\"keyword\": \"Examples:\",\"steps\": [],"
                + "\"parameters\": {\"names\": [\"key\",\"row\"],\"values\": [[\"key1\",\"row1\"],[\"key2\",\"row2\"]]},"
                + "\"examples\": [{\"keyword\": \"Example:\", \"parameters\": {}}]},\"start\":1,\"end\":2}]}";

        assertThat(dos2unix(out.toString()), equalTo(expected));
    }

    @Test
    void shouldNotSuppressStackTraceForNotKnownFailure() {

        // Given
        final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            @Override
            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
        StoryReporter reporter = new TxtOutput(factory.createPrintStream(), new Properties(), new LocalizedKeywords(), true);


        reporter.failed("Then I should have a balance of $30", new UUIDExceptionWrapper(new NullPointerException()));
        reporter.afterScenario(getTiming());

        assertThat(dos2unix(out.toString()), startsWith("Then I should have a balance of $30 (FAILED)\n" +
                "(java.lang.NullPointerException)\n" +
                "\n" +
                "java.lang.NullPointerException\n" +
                "\tat "));

    }

    @Test
    void shouldSuppressStackTraceForKnownFailure() {
        // Given
        final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            @Override
            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
        StoryReporter reporter = new TxtOutput(factory.createPrintStream(), new Properties(), new LocalizedKeywords(), true);


        reporter.failed("Then I should have a balance of $30", new UUIDExceptionWrapper(new MyKnownFailure()));
        reporter.afterScenario(getTiming());

        assertThat(dos2unix(out.toString()), equalTo("Then I should have a balance of $30 (FAILED)\n" +
                "(org.jbehave.core.reporters.PrintStreamOutputBehaviour$MyKnownFailure)\n\n" +
                ""));
    }

    @Test
    void shouldReportFailureTraceWhenToldToDoSo() {
        // Given
        UUIDExceptionWrapper exception = new UUIDExceptionWrapper(new RuntimeException("Leave my money alone!"));
        OutputStream stackTrace = new ByteArrayOutputStream();
        exception.getCause().printStackTrace(new PrintStream(stackTrace));
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new TxtOutput(new PrintStream(out), new Properties(),
                new LocalizedKeywords(), true);

        // When
        reporter.beforeScenario(spyScenarioUuid(new Scenario("A title", Meta.EMPTY)));
        reporter.successful("Given I have a balance of $50");
        reporter.successful("When I request $20");
        reporter.failed("When I ask Liz for a loan of $100", exception);
        reporter.pending("Then I should have a balance of $30");
        reporter.notPerformed("Then I should have $20");
        reporter.afterScenario(getTiming());

        // Then
        String expected = "Scenario: A title\n" 
                + "Given I have a balance of $50\n"
                + "When I request $20\n"
                + "When I ask Liz for a loan of $100 (FAILED)\n"
                + "(java.lang.RuntimeException: Leave my money alone!)\n"
                + "Then I should have a balance of $30 (PENDING)\n"
                + "Then I should have $20 (NOT PERFORMED)\n" 
                + "\n";
        String actual = dos2unix(out.toString());
        assertThat(actual, containsString(expected));
        assertThat(actual, containsString("at org.jbehave.core.reporters.PrintStreamOutputBehaviour.shouldReportFailureTraceWhenToldToDoSo("));


        // Given
        out = new ByteArrayOutputStream();
        reporter = new TxtOutput(new PrintStream(out));

        // When
        reporter.beforeScenario(new Scenario("A title", Meta.EMPTY));
        reporter.successful("Given I have a balance of $50");
        reporter.successful("When I request $20");
        reporter.failed("When I ask Liz for a loan of $100", exception);
        reporter.pending("Then I should have a balance of $30");
        reporter.notPerformed("Then I should have $20");
        reporter.afterScenario(getTiming());

        // Then
        assertThat(out.toString().contains(stackTrace.toString()), is(false));
    }

    @Test
    void shouldReportEventsToIdeOnlyConsoleOutput() {
        // When
        StoryNarrator.narrateAnInterestingStory(new IdeOnlyConsoleOutput(), false);
        StoryNarrator.narrateAnInterestingStory(new IdeOnlyConsoleOutput(new LocalizedKeywords()), false);
        StoryNarrator.narrateAnInterestingStory(new IdeOnlyConsoleOutput(new Properties(), new LocalizedKeywords(), true), false);
    }

    @Test
    void shouldReportEventsToPrintStreamInItalian() {
        // Given
        UUIDExceptionWrapper exception = new UUIDExceptionWrapper(new RuntimeException("Lasciate in pace i miei soldi!"));
        OutputStream out = new ByteArrayOutputStream();
        LocalizedKeywords keywords = new LocalizedKeywords(Locale.ITALIAN);
        StoryReporter reporter = new TxtOutput(new PrintStream(out), new Properties(), keywords,
                true);

        // When
        reporter.successful("Dato che ho un saldo di $50");
        reporter.successful("Quando richiedo $20");
        reporter.failed("Quando chiedo a Liz un prestito di $100", exception);
        reporter.pending("Allora dovrei avere un saldo di $30");
        reporter.notPerformed("Allora dovrei avere $20");

        // Then
        String expected = "Dato che ho un saldo di $50\n" 
                + "Quando richiedo $20\n"
                + "Quando chiedo a Liz un prestito di $100 (FALLITO)\n"
                + "(java.lang.RuntimeException: Lasciate in pace i miei soldi!)\n"
                + "Allora dovrei avere un saldo di $30 (IN SOSPESO)\n"
                + "Allora dovrei avere $20 (NON ESEGUITO)\n";

        assertThat(dos2unix(out.toString()), equalTo(expected));

    }

    @Test
    void shouldCreateAndWriteToFilePrintStreamForStoryLocation() throws IOException {

        // Given
        String storyPath = storyPath(MyStory.class);
        FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(CodeLocations.codeLocationFromClass(this.getClass()), storyPath));
        File file = factory.outputFile();
        file.delete();
        assertThat(file.exists(), is(false));
        
        // When
        PrintStream printStream = factory.createPrintStream();
        file = factory.getOutputFile();
        printStream.print("Hello World");

        // Then
        assertThat(file.exists(), is(true));
        assertThat(IOUtils.toString(new FileReader(file), true), equalTo("Hello World"));
    }

    @Test
    void shouldReportEventsToFilePrintStreamsAndGenerateView() throws IOException {
        final String storyPath = storyPath(MyStory.class);
        File outputDirectory = new File("target/output");
        StoryReporter reporter = new StoryReporterBuilder().withRelativeDirectory(outputDirectory.getName())
                .withFormats(HTML, TXT)
                .build(storyPath);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);
        ViewGenerator viewGenerator = new FreemarkerViewGenerator();
        Properties viewProperties = new Properties();
        viewGenerator.generateReportsView(outputDirectory, asList("html", "txt"), viewProperties);

        // Then
        ensureFileExists(new File(outputDirectory, "view/index.html"));
        ensureFileExists(new File(outputDirectory, "view/org.jbehave.core.reporters.my_story.txt.html"));
    }

    @Test
    void shouldReportEventsToFilePrintStreamsAndGenerateViewWithoutDecoratingNonHtml() throws IOException {
        final String storyPath = storyPath(MyStory.class);
        File outputDirectory = new File("target/output");
        StoryReporter reporter = new StoryReporterBuilder().withRelativeDirectory(outputDirectory.getName())
                .withFormats(HTML, TXT)
                .build(storyPath);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);
        ((ConcurrentStoryReporter) reporter).invokeDelayed();
        ViewGenerator viewGenerator = new FreemarkerViewGenerator();
        Properties viewProperties = new Properties();
        viewProperties.setProperty("decorateNonHtml", "false");
        viewGenerator.generateReportsView(outputDirectory, asList("html", "txt"), viewProperties);

        // Then
        ensureFileExists(new File(outputDirectory, "view/index.html"));
        ensureFileExists(new File(outputDirectory, "view/org.jbehave.core.reporters.my_story.txt"));
    }

    
    @Test
    void shouldBuildPrintStreamReportersAndOverrideDefaultForAGivenFormat() throws IOException {
        final String storyPath = storyPath(MyStory.class);
        final FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(CodeLocations.codeLocationFromClass(this.getClass()), storyPath));
        StoryReporter reporter = new StoryReporterBuilder() {
            @Override
            public StoryReporter reporterFor(String storyPath, org.jbehave.core.reporters.Format format) {
                if (format == org.jbehave.core.reporters.Format.TXT) {
                    factory.useConfiguration(new FilePrintStreamFactory.FileConfiguration("text"));
                    return new TxtOutput(factory.createPrintStream(), new Properties(), new LocalizedKeywords(), true);
                } else {
                    return super.reporterFor(storyPath, format);
                }
            }
        }.withFormats(TXT).build(storyPath);

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);
        ((ConcurrentStoryReporter) reporter).invokeDelayed();


        // Then
        File outputFile = factory.getOutputFile();
        ensureFileExists(outputFile);
    }

    private void ensureFileExists(File file) throws IOException {
        assertThat(file.exists(), is(true));
        assertThat(IOUtils.toString(new FileReader(file), true).length(), greaterThan(0));
    }

    private String storyPath(Class<MyStory> storyClass) {
        StoryPathResolver resolver = new UnderscoredCamelCaseResolver(".story");
        return resolver.resolve(storyClass);
    }

    @Test
    void shouldUseCustomDateFormatInOutcomesTable() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new TxtOutput(new PrintStream(out));

        // When
        OutcomesTable outcomesTable = new OutcomesTable(new LocalizedKeywords(), mapOf(Date.class, "dd/MM/yyyy"));
        Date actualDate = StoryNarrator.dateFor("01/01/2011");
        Date expectedDate = StoryNarrator.dateFor("02/01/2011");
        outcomesTable.addOutcome("A wrong date", actualDate, new IsDateEqual(expectedDate, outcomesTable.getFormat(
            Date.class)));
        try {
            outcomesTable.verify();
        } catch (UUIDExceptionWrapper e) {
            reporter.failedOutcomes("some step", ((OutcomesFailed) e.getCause()).outcomesTable());
        }

        // Then
        String expected = "some step (FAILED)\n"
                + "(org.jbehave.core.model.OutcomesTable$OutcomesFailed)\n" 
                + "|Description|Value|Matcher|Verified|\n"
                + "|A wrong date|01/01/2011|\"02/01/2011\"|No|\n";
        assertThat(dos2unix(out.toString()), equalTo(expected));
    }

    private Map<Type, String> mapOf(Type type, String value) {
        Map<Type,String> map = new HashMap<>();
        map.put(type, value);
        return map;
    }

    @Test
    void shouldReportEventsToJsonOutputEmptyScenarioLifecycle() throws IOException {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new JsonOutput(new PrintStream(out), new Properties(), new LocalizedKeywords());
        String scenarioStep = "Then '((some data))' is ((equal to)) '((some data))'";

        // When
        ExamplesTable table = new ExamplesTable("|actual|expected|\n|some data|some data|\n");
        Lifecycle lifecycle = new Lifecycle(table);
        ExamplesTable emptyExamplesTable = ExamplesTable.EMPTY;
        Story story = spyStoryUuid(new Story("/path/to/story", new Description("Story with lifecycle and empty scenario"), null,
                null, null, lifecycle, new ArrayList<Scenario>()));
        Timing timing = getTiming();

        reporter.beforeStory(story, false);
        reporter.lifecyle(lifecycle);
        reporter.beforeScenarios();
        reporter.beforeScenario(spyScenarioUuid(new Scenario("Normal scenario", Meta.EMPTY)));
        reporter.beforeExamples(Collections.singletonList("Then '<expected>' is equal to '<actual>'"), emptyExamplesTable);
        reporter.example(table.getRow(0), -1);
        reportStep(reporter, scenarioStep, Stage.BEFORE);
        reportStep(reporter, scenarioStep, null);
        reportStep(reporter, scenarioStep, Stage.AFTER);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.beforeScenario(spyScenarioUuid(new Scenario("Some empty scenario", Meta.EMPTY)));
        reporter.beforeExamples(Collections.<String>emptyList(), emptyExamplesTable);
        reporter.example(table.getRow(0), -1);
        reportStep(reporter, scenarioStep, Stage.BEFORE);
        reportStep(reporter, scenarioStep, Stage.AFTER);
        reporter.afterExamples();
        reporter.afterScenario(timing);
        reporter.afterScenarios();
        reporter.afterStory(false);

        // Then

        assertJson("story-empty-scenario-with-lifecycle.json", out.toString());
    }

    private void assertJson(String expectedJsonFileName, String actualJson) throws IOException {
        String expected = IOUtils.toString(getClass().getResourceAsStream("/" + expectedJsonFileName), true);
        JsonParser parser = new JsonParser();
        JsonObject expectedObject = parser.parse(actualJson).getAsJsonObject();
        JsonObject actualObject = parser.parse(expected).getAsJsonObject();
        assertThat(expectedObject, is(actualObject));
    }

    private void reportStep(StoryReporter reporter, String step, Stage stage) {
        reporter.beforeScenarioSteps(stage);
        reporter.beforeStep(step);
        reporter.successful(step);
        reporter.afterScenarioSteps(stage);
    }

    private Timing getTiming() {
        Timing timing = mock(Timing.class);
        when(timing.getStart()).thenReturn(1l);
        when(timing.getEnd()).thenReturn(2l);
        return timing;
    }

    private static Scenario spyScenarioUuid(Scenario scenario) {
        Scenario spy = spy(scenario);
        when(spy.getId()).thenReturn("scenario-id");
        return spy;
    }

    private static Story spyStoryUuid(Story story) {
        Story spy = spy(story);
        when(spy.getId()).thenReturn("story-id");
        return spy;
    }

    @SuppressWarnings("serial")
    private static class MyKnownFailure extends KnownFailure {
    }

    private abstract class MyStory extends JUnitStory {

    }

    private PrintStream createPrintStream(File file) throws FileNotFoundException {
        return new PrintStream(new FilePrintStreamFactory.FilePrintStream(file,true));
    }


}
