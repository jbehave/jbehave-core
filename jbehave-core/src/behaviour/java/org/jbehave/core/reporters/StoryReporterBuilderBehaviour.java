package org.jbehave.core.reporters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.parser.StoryLocation;
import org.jbehave.core.parser.StoryPathResolver;
import org.jbehave.core.parser.UnderscoredCamelCaseResolver;
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
    public void shouldAllowOverrideOfDefaultOuputDirectory() throws IOException {
    	
    	// Given
        StoryReporterBuilder builder = new StoryReporterBuilder();
        String storyPath = storyPath(MyStory.class);

        // When
        String outputDirectory = "my-reports";
        builder.outputTo(outputDirectory).outputAsAbsolute(true).build(storyPath);
        
        // Then
        assertThat(builder.fileConfiguration("").getOutputDirectory(), equalTo((outputDirectory)));
        assertThat(builder.fileConfiguration("").isOutputDirectoryAbsolute(),  is(true));
    }

    @Test
    public void shouldBuildAndOverrideDefaultReporterForAGivenFormat() throws IOException {
    	// Given
        String storyPath = storyPath(MyStory.class);
        final FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(storyPath, MyStory.class));
        final StoryReporter txtReporter = new PrintStreamOutput(factory.createPrintStream(), new Properties(),  new LocalizedKeywords(), true);
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
