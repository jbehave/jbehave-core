package org.jbehave.core.reporters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.i18n.StringCoder;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.FilePrintStreamFactory.FileConfiguration;
import org.junit.Test;

public class StoryReporterBuilderBehaviour {

    @Test
    public void shouldBuildWithStatsByDefault() throws IOException {
    	// Given
        StoryReporterBuilder builder = new StoryReporterBuilder();
        String storyPath = storyPath(MyStory.class);

        // When
        StoryReporter reporter = builder.withDefaultFormats().build(storyPath);
        
        // Then
        assertThat(reporter, instanceOf(DelegatingStoryReporter.class));
        Collection<StoryReporter> delegates = ((DelegatingStoryReporter)reporter).getDelegates();
        assertThat(delegates.size(), equalTo(1));
        assertThat(delegates.iterator().next(), instanceOf(PostStoryStatisticsCollector.class));
    }

    @Test
    public void shouldBuildWithCustomOuputDirectory() throws IOException {
    	
    	// Given
        StoryReporterBuilder builder = new StoryReporterBuilder();
        String storyPath = storyPath(MyStory.class);

        // When
        String outputDirectory = "my-reports";
        builder.withOutputDirectory(outputDirectory).build(storyPath);
        
        // Then
        assertThat(builder.fileConfiguration("").getOutputDirectory(), equalTo((outputDirectory)));
    }

    @Test
    public void shouldBuildWithReportingOfFailureTrace() throws IOException {    	
    	// Given
        StoryReporterBuilder builder = new StoryReporterBuilder();
        String storyPath = storyPath(MyStory.class);
        // When
        StoryReporter reporter = builder.withFormats(TXT).withFailureTrace(true).build(storyPath);
        
        // Then
        assertThat(reporter, instanceOf(DelegatingStoryReporter.class));
        Collection<StoryReporter> delegates = ((DelegatingStoryReporter)reporter).getDelegates();
        assertThat(delegates.size(), equalTo(1));
        StoryReporter storyReporter = delegates.iterator().next();
		assertThat(storyReporter, instanceOf(TxtOutput.class));
		assertThat(storyReporter.toString(), containsString("reportFailureTrace=true"));
    }    

    @Test
    public void shouldBuildWithCustomKeywords() throws IOException {
        // Given
        String storyPath = storyPath(MyStory.class);
        LocalizedKeywords keywords = new LocalizedKeywords(new Locale("it"),
                "org/jbehave/core/i18n/keywords", this.getClass().getClassLoader(), new StringCoder());
        final URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        final OutputStream out = new ByteArrayOutputStream();

        StoryReporterBuilder builder = new StoryReporterBuilder(){
            @Override
            protected FilePrintStreamFactory filePrintStreamFactory(String storyPath) {
                return new FilePrintStreamFactory(new StoryLocation(codeLocation, storyPath)){
                    @Override
                    public PrintStream createPrintStream() {
                        return new PrintStream(out);
                    }                    
                };
            }            
        };

        // When
        StoryReporter reporter = builder.withDefaultFormats().withFormats(TXT).withKeywords(keywords).build(storyPath);
        reporter.failed("Dato un passo che fallisce", new RuntimeException("ouch"));
        
        // Then
        assertThat(out.toString(), equalTo("Dato un passo che fallisce (FALLITO)\n(java.lang.RuntimeException: ouch)\n"));
    }

    @Test
    public void shouldBuildWithCustomReporterForAGivenFormat() throws IOException {
    	// Given
        String storyPath = storyPath(MyStory.class);
        final FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(CodeLocations.codeLocationFromClass(MyStory.class), storyPath));
        final StoryReporter txtReporter = new TxtOutput(factory.createPrintStream(), new Properties(),  new LocalizedKeywords(), true);
        StoryReporterBuilder builder = new StoryReporterBuilder(){
               public StoryReporter reporterFor(String storyPath, Format format){
                       switch (format) {
                           case TXT:
                               factory.useConfiguration(new FileConfiguration("text"));
                               return txtReporter;
                            default:
                               return super.reporterFor(storyPath, format);
                       }
                   }
        };
        
        // When
        StoryReporter reporter = builder.withDefaultFormats().withFormats(TXT).build(storyPath);
        
        // Then
        assertThat(reporter, instanceOf(DelegatingStoryReporter.class));
        Collection<StoryReporter> delegates = ((DelegatingStoryReporter)reporter).getDelegates();
        assertThat(delegates.size(), equalTo(2));
        assertThat(delegates.contains(txtReporter), is(true));
    }

    private String storyPath(Class<MyStory> storyClass) {
        StoryPathResolver resolver = new UnderscoredCamelCaseResolver(".story");
        return resolver.resolve(storyClass);
    }


    private static class MyStory extends JUnitStory {

    }
}
