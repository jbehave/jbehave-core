package org.jbehave.core.reporters;

import org.jbehave.core.failures.KnownFailure;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.*;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.reporters.StoryNarrator.IsDateEqual;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;

public class PrintStreamOutputBehaviour extends AbstractOutputBehaviour {

    @Test
    public void shouldOutputStoryToTxt() throws IOException {
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
    public void shouldOutputStoryToTxtWhenNotAllowedByFilter() throws IOException {
        // Given
        String name = "stream-story-not-allowed.txt";
        File file = newFile("target/"+ name);
        StoryReporter reporter = new TxtOutput(createPrintStream(file));

        // When
        StoryNarrator
                .narrateAnInterestingStoryNotAllowedByFilter(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    public void shouldOutputStoryToTxtUsingCustomPatterns() throws IOException {
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
    public void shouldOutputStoryToHtml() throws IOException {
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
    public void shouldOutputStoryToHtmlWhenNotAllowedByFilter() throws IOException {
        // Given
        String name = "stream-story-not-allowed.html";
        File file = newFile("target/" + name);
        StoryReporter reporter = new HtmlOutput(createPrintStream(file));

        // When
        StoryNarrator
                .narrateAnInterestingStoryNotAllowedByFilter(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    public void shouldOutputStoryToHtmlUsingCustomPatterns() throws IOException {
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
    public void shouldOutputStoryToXml() throws IOException, SAXException {
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
    public void shouldOutputStoryToXmlWhenNotAllowedByFilter() throws IOException {
        // Given
        String name = "stream-story-not-allowed.xml";
        File file = newFile("target/" + name);
        StoryReporter reporter = new XmlOutput(createPrintStream(file));

        // When
        StoryNarrator
                .narrateAnInterestingStoryNotAllowedByFilter(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
    }

    @Test
    public void shouldOutputStoryToXmlUsingCustomPatterns() throws IOException {
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
    public void shouldOutputStoryJson() throws IOException, SAXException {
        // Given
        String name = "stream-story.json";
        File file = new File("target/" + name);
        StoryReporter reporter = new JsonOutput(new FilePrintStreamFactory.FilePrintStream(file, false),
                new LocalizedKeywords());

        // When
        StoryNarrator.narrateAnInterestingStory(reporter, false);

        // Then
        assertFileOutputIsSameAs(file, name);
        validateFileOutput(file);
    }

    @Test
    public void shouldNotSuppressStackTraceForNotKnownFailure() {

        // Given
        final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            @Override
            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
        TxtOutput reporter = new TxtOutput(factory.createPrintStream(), new Properties(), new LocalizedKeywords(), true);


        reporter.failed("Then I should have a balance of $30", new UUIDExceptionWrapper(new NullPointerException()));
        reporter.afterScenario();

        assertThat(dos2unix(out.toString()), startsWith("Then I should have a balance of $30 (FAILED)\n" +
                "(java.lang.NullPointerException)\n" +
                "\n" +
                "java.lang.NullPointerException\n" +
                "\tat "));

    }

    @Test
    public void shouldSuppressStackTraceForKnownFailure() {
        // Given
        final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            @Override
            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
        TxtOutput reporter = new TxtOutput(factory.createPrintStream(), new Properties(), new LocalizedKeywords(), true);


        reporter.failed("Then I should have a balance of $30", new UUIDExceptionWrapper(new MyKnownFailure()));
        reporter.afterScenario();

        assertThat(dos2unix(out.toString()), equalTo("Then I should have a balance of $30 (FAILED)\n" +
                "(org.jbehave.core.reporters.PrintStreamOutputBehaviour$MyKnownFailure)\n\n" +
                ""));
    }

    @Test
    public void shouldReportFailureTraceWhenToldToDoSo() {
        // Given
        UUIDExceptionWrapper exception = new UUIDExceptionWrapper(new RuntimeException("Leave my money alone!"));
        OutputStream stackTrace = new ByteArrayOutputStream();
        exception.getCause().printStackTrace(new PrintStream(stackTrace));
        OutputStream out = new ByteArrayOutputStream();
        TxtOutput reporter = new TxtOutput(new PrintStream(out), new Properties(),
                new LocalizedKeywords(), true);

        // When
        reporter.beforeScenario(new Scenario("A title", Meta.EMPTY));
        reporter.successful("Given I have a balance of $50");
        reporter.successful("When I request $20");
        reporter.failed("When I ask Liz for a loan of $100", exception);
        reporter.pending("Then I should have a balance of $30");
        reporter.notPerformed("Then I should have $20");
        reporter.afterScenario();

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
        reporter.afterScenario();

        // Then
        assertThat(out.toString().contains(stackTrace.toString()), is(false));
    }

    @Test
    public void shouldReportEventsToIdeOnlyConsoleOutput() {
        // When
        StoryNarrator.narrateAnInterestingStory(new IdeOnlyConsoleOutput(), false);
        StoryNarrator.narrateAnInterestingStory(new IdeOnlyConsoleOutput(new LocalizedKeywords()), false);
        StoryNarrator.narrateAnInterestingStory(new IdeOnlyConsoleOutput(new Properties(), new LocalizedKeywords(), true), false);
    }

    @Test
    public void shouldReportEventsToPrintStreamInItalian() {
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
    public void shouldCreateAndWriteToFilePrintStreamForStoryLocation() throws IOException {

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
    public void shouldReportEventsToFilePrintStreamsAndGenerateView() throws IOException {
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
    public void shouldReportEventsToFilePrintStreamsAndGenerateViewWithoutDecoratingNonHtml() throws IOException {
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
    public void shouldBuildPrintStreamReportersAndOverrideDefaultForAGivenFormat() throws IOException {
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
    public void shouldUseCustomDateFormatInOutcomesTable() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StoryReporter reporter = new TxtOutput(new PrintStream(out));

        // When
        OutcomesTable outcomesTable = new OutcomesTable(new LocalizedKeywords(), "dd/MM/yyyy");
        Date actualDate = StoryNarrator.dateFor("01/01/2011");
        Date expectedDate = StoryNarrator.dateFor("02/01/2011");
        outcomesTable.addOutcome("A wrong date", actualDate, new IsDateEqual(expectedDate, outcomesTable.getDateFormat()));
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

    @SuppressWarnings("serial")
    private static class MyKnownFailure extends KnownFailure {
    }

    private abstract class MyStory extends JUnitStory {

    }

    private PrintStream createPrintStream(File file) throws FileNotFoundException {
        return new PrintStream(new FilePrintStreamFactory.FilePrintStream(file,true));
    }


}
