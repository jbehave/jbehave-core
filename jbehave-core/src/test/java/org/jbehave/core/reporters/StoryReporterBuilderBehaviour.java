package org.jbehave.core.reporters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToPackagedName;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToSimpleName;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.JSON;
import static org.jbehave.core.reporters.Format.IDE_CONSOLE;
import static org.jbehave.core.reporters.Format.STATS;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

public class StoryReporterBuilderBehaviour {

    @Test
    public void shouldBuildWithStatsByDefault() throws IOException {
        // Given
        StoryReporterBuilder builder = new StoryReporterBuilder();
        String storyPath = storyPath(MyStory.class);

        // When
        StoryReporter reporter = builder.withDefaultFormats().build(storyPath);

        // Then
        assertThat(reporter, instanceOf(ConcurrentStoryReporter.class));
        StoryReporter delegate = ((ConcurrentStoryReporter) reporter).getDelegate();
        assertThat(delegate, instanceOf(DelegatingStoryReporter.class));
        Collection<StoryReporter> delegates = ((DelegatingStoryReporter) delegate).getDelegates();
        assertThat(delegates.size(), equalTo(1));
        assertThat(delegates.iterator().next(), instanceOf(PostStoryStatisticsCollector.class));
    }

    @Test
    public void shouldBuildWithCustomRelativeDirectory() throws IOException {
        // Given
        StoryReporterBuilder builder = new StoryReporterBuilder();
        String storyPath = storyPath(MyStory.class);

        // When
        String relativeDirectory = "my-reports";
        builder.withRelativeDirectory(relativeDirectory).build(storyPath);

        // Then
        assertThat(builder.relativeDirectory(), equalTo((relativeDirectory)));
        assertThat(builder.fileConfiguration("").getRelativeDirectory(), equalTo((relativeDirectory)));
    }

    @Test
    public void shouldBuildWithCustomPathResolver() throws IOException {
        // Given
        StoryReporterBuilder builder = new StoryReporterBuilder();
        String storyPath = storyPath(MyStory.class);

        // When
        assertThat(builder.pathResolver(), instanceOf(ResolveToPackagedName.class));
        builder.withPathResolver(new ResolveToSimpleName()).build(storyPath);

        // Then
        assertThat(builder.pathResolver(), instanceOf(ResolveToSimpleName.class));
        assertThat(builder.fileConfiguration("").getPathResolver(), instanceOf(ResolveToSimpleName.class));
    }

    @Test
    public void shouldBuildWithReportingOfFailureTrace() throws IOException {
        // Given
        StoryReporterBuilder builder = new StoryReporterBuilder();
        String storyPath = storyPath(MyStory.class);
        // When
        StoryReporter reporter = builder.withFormats(TXT).withFailureTrace(true).build(storyPath);

        // Then
        assertThat(builder.reportFailureTrace(), is(true));
        assertThat(reporter, instanceOf(ConcurrentStoryReporter.class));
        StoryReporter delegate = ((ConcurrentStoryReporter) reporter).getDelegate();
        assertThat(delegate, instanceOf(DelegatingStoryReporter.class));
        Collection<StoryReporter> delegates = ((DelegatingStoryReporter) delegate).getDelegates();
        assertThat(delegates.size(), equalTo(1));
        StoryReporter storyReporter = delegates.iterator().next();
        assertThat(storyReporter, instanceOf(TxtOutput.class));
        assertThat(((TxtOutput) storyReporter).reportFailureTrace(), is(true));
        assertThat(((TxtOutput) storyReporter).compressFailureTrace(), is(false));
    }

    @Test
    public void shouldBuildWithCustomCodeLocation() throws IOException {
        // Given
        StoryReporterBuilder builder = new StoryReporterBuilder();
        String storyPath = storyPath(MyStory.class);
        URL codeLocation = CodeLocations.codeLocationFromPath("target/custom/location");
        // When
        builder.withFormats(TXT).withCodeLocation(codeLocation).build(storyPath);

        // Then
        assertThat(builder.codeLocation(), equalTo(codeLocation));
        FilePrintStreamFactory factory = builder.filePrintStreamFactory(storyPath);
        assertThat(factory.outputDirectory().getPath().replace('\\', '/'), endsWith("custom/"
                + factory.configuration().getRelativeDirectory()));

    }

    @Test
    public void shouldBuildWithCustomViewResources() throws IOException {
        // Given
        String storyPath = storyPath(MyStory.class);
        StoryReporterBuilder builder = new StoryReporterBuilder();

        Properties resources = new Properties();

        // When
        builder.withDefaultFormats().withFormats(TXT).withViewResources(resources).build(storyPath);

        // Then
        assertThat(builder.viewResources(), equalTo(resources));
    }

    @Test
    public void shouldBuildWithCustomKeywords() throws IOException {
        // Given
        String storyPath = storyPath(MyStory.class);
        Keywords keywords = new LocalizedKeywords(new Locale("it"));
        final OutputStream out = new ByteArrayOutputStream();

        StoryReporterBuilder builder = new StoryReporterBuilder() {
            @Override
            protected FilePrintStreamFactory filePrintStreamFactory(String storyPath) {
                return new FilePrintStreamFactory(new StoryLocation(codeLocation(), storyPath)) {
                    @Override
                    public PrintStream createPrintStream() {
                        return new PrintStream(out);
                    }
                };
            }
        };

        // When
        StoryReporter reporter = builder.withDefaultFormats().withFormats(TXT).withKeywords(keywords).build(storyPath);
        reporter.failed("Dato un passo che fallisce", new UUIDExceptionWrapper(new RuntimeException("ouch")));

        ((ConcurrentStoryReporter) reporter).invokeDelayed();

        // Then
        assertThat(builder.keywords(), equalTo(keywords));
        assertThat(out.toString(),
                equalTo("Dato un passo che fallisce (FALLITO)\n(java.lang.RuntimeException: ouch)\n"));
    }

    @Test
    public void shouldBuildWithReporterOfDifferentFormatsForSingleThreaded() throws IOException {

        StoryReporterBuilder builder = new StoryReporterBuilder().withMultiThreading(false);
        shouldBuildWithReporterOfDifferentFormats(builder);

    }

    @Test
    public void shouldBuildWithReporterOfDifferentFormatsForMultiThreaded() throws IOException {

        StoryReporterBuilder builder = new StoryReporterBuilder().withMultiThreading(true);
        shouldBuildWithReporterOfDifferentFormats(builder);

    }

    private void shouldBuildWithReporterOfDifferentFormats(StoryReporterBuilder builder) {
        // Given
        String storyPath = storyPath(MyStory.class);
        Locale locale = Locale.getDefault();

        // When
        Format[] formats = { CONSOLE, IDE_CONSOLE, HTML, STATS, TXT, XML, JSON };
        StoryReporter reporter = builder.withDefaultFormats().withFormats(formats)
                .withKeywords(new LocalizedKeywords(locale)).build(storyPath);

        // Then
        assertThat(builder.formats(), hasItems(CONSOLE, IDE_CONSOLE, HTML, STATS, TXT, XML, JSON));
        String[] upperCaseNames = new String[] { "CONSOLE", "IDE_CONSOLE", "HTML", "STATS", "TXT", "XML", "JSON" };
        assertThat(builder.formatNames(false), hasItems(upperCaseNames));
        String[] lowerCaseNames = new String[formats.length];
        for (int i = 0; i < upperCaseNames.length; i++) {
            lowerCaseNames[i] = upperCaseNames[i].toLowerCase(locale);
        }
        assertThat(builder.formatNames(true), hasItems(lowerCaseNames));
        assertThat(reporter, instanceOf(ConcurrentStoryReporter.class));
        StoryReporter delegate = ((ConcurrentStoryReporter) reporter).getDelegate();
        assertThat(delegate, instanceOf(DelegatingStoryReporter.class));
        Collection<StoryReporter> delegates = ((DelegatingStoryReporter) delegate).getDelegates();
        assertThat(delegates.size(), equalTo(formats.length));

    }

    @Test
    public void shouldBuildWithCustomReporterForAGivenFormat() throws IOException {
        // Given
        String storyPath = storyPath(MyStory.class);
        final FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(
                CodeLocations.codeLocationFromClass(MyStory.class), storyPath));
        final StoryReporter txtReporter = new TxtOutput(factory.createPrintStream(), new Properties(),
                new LocalizedKeywords(), true);
        StoryReporterBuilder builder = new StoryReporterBuilder() {
            @Override
            public StoryReporter reporterFor(String storyPath, org.jbehave.core.reporters.Format format) {
                if (format == org.jbehave.core.reporters.Format.TXT) {
                    factory.useConfiguration(new FilePrintStreamFactory.FileConfiguration("text"));
                    return txtReporter;
                } else {
                    return super.reporterFor(storyPath, format);
                }
            }
        };

        // When
        StoryReporter reporter = builder.withDefaultFormats().withFormats(TXT).build(storyPath);

        // Then
        assertThat(reporter, instanceOf(ConcurrentStoryReporter.class));
        StoryReporter delegate = ((ConcurrentStoryReporter) reporter).getDelegate();
        assertThat(delegate, instanceOf(DelegatingStoryReporter.class));
        Collection<StoryReporter> delegates = ((DelegatingStoryReporter) delegate).getDelegates();
        assertThat(delegates.size(), equalTo(2));
        assertThat(delegates.contains(txtReporter), is(true));
    }

    @Test
    public void shouldBuildWithCustomReportersAsProvidedFormat() throws IOException {
        // Given
        String storyPath = storyPath(MyStory.class);
        FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(
                CodeLocations.codeLocationFromClass(MyStory.class), storyPath));
        StoryReporter txtReporter = new TxtOutput(factory.createPrintStream(), new Properties(),
                new LocalizedKeywords(), true);
        StoryReporter htmlReporter = new TxtOutput(factory.createPrintStream(), new Properties(),
                new LocalizedKeywords(), true);
        StoryReporterBuilder builder = new StoryReporterBuilder();

        // When
        StoryReporter reporter = builder.withReporters(txtReporter, htmlReporter).build(storyPath);

        // Then
        assertThat(reporter, instanceOf(ConcurrentStoryReporter.class));
        StoryReporter delegate = ((ConcurrentStoryReporter) reporter).getDelegate();
        assertThat(delegate, instanceOf(DelegatingStoryReporter.class));
        Collection<StoryReporter> delegates = ((DelegatingStoryReporter) delegate).getDelegates();
        assertThat(delegates.size(), equalTo(2));
        assertThat(delegates.contains(txtReporter), is(true));
        assertThat(delegates.contains(htmlReporter), is(true));
    }

    private String storyPath(Class<MyStory> storyClass) {
        StoryPathResolver resolver = new UnderscoredCamelCaseResolver(".story");
        return resolver.resolve(storyClass);
    }

    private static class MyStory extends JUnitStory {

    }
}
