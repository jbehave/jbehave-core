package org.jbehave.core.embedder;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.PrintStreamOutput.Format;
import org.jbehave.core.reporters.ReportsCount;
import org.junit.Test;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EmbedderMonitorBehaviour {

    @Test
    public void shouldNotPrintWithSilentMonitor() throws Throwable {
        OutputStream out = new ByteArrayOutputStream();
        SilentEmbedderMonitor monitor = new SilentEmbedderMonitor();
        monitor.print("a message");
        monitor.printStackTrace(new RuntimeException());
        assertThat(out.toString(), is(""));
    }

    @Test
    public void shouldOnlyPrintFailuresWithReportingFailuresMonitor() throws Throwable {
        OutputStream out = new ByteArrayOutputStream();
        ReportingFailuresEmbedderMonitor monitor = new ReportingFailuresEmbedderMonitor(new PrintStream(out));
        monitor.runningEmbeddable("embeddable");
        monitor.runningStory("/path");
        monitor.generatingReportsView(new File("target"), Arrays.asList(Format.HTML.name()), new Properties());
        monitor.reportsViewGenerated(new ReportsCount(1, 0, 1, 2, 1, 0, 1, 1));
        assertThat(out.toString(), is(""));
        monitor.batchFailed(new BatchFailures());
        monitor.storyFailed("/path", new RuntimeException());
        assertThat(out.toString(), is(not("")));
   }

    @Test
    public void shouldDelegateOutput() throws Throwable {
        EmbedderMonitor monitor = new ReportingFailuresEmbedderMonitor();
        assertThat(monitor.toString(), containsString(ReportingFailuresEmbedderMonitor.class.getSimpleName()+"[output="));
   }


    @Test
    public void shouldAllowDecorationOfDelegate() throws Throwable {
        // Given
        EmbedderMonitor delegate = mock(EmbedderMonitor.class);
        EmbedderMonitor monitor = new EmbedderMonitorDecorator(delegate);
        
        // When
        Object annotatedInstance = new Object();
        monitor.annotatedInstanceNotOfType(annotatedInstance, annotatedInstance.getClass());
        BatchFailures failures = new BatchFailures();
        monitor.batchFailed(failures);
        monitor.beforeOrAfterStoriesFailed();
        String name = "name";
        Throwable cause = new Throwable();
        monitor.embeddableFailed(name, cause);
        monitor.embeddableNotConfigurable(name);
        List<String> names = asList("name1");
        monitor.embeddablesSkipped(names);
        File outputDirectory = new File("target");
        StoryMaps storyMaps = mock(StoryMaps.class);
        Properties viewProperties = mock(Properties.class);
        monitor.generatingMapsView(outputDirectory, storyMaps, viewProperties);
        Properties viewResources = mock(Properties.class);
        monitor.generatingNavigatorView(outputDirectory, viewResources);
        List<String> formats = asList("TXT");
        monitor.generatingReportsView(outputDirectory, formats, viewProperties);
        String storyPath = "path";
        List<String> metaFilters = asList("- skip");
        monitor.mappingStory(storyPath, metaFilters);
        monitor.mapsViewGenerationFailed(outputDirectory, storyMaps, viewProperties, cause);
        Meta meta = mock(Meta.class);
        MetaFilter filter = mock(MetaFilter.class);
        monitor.metaNotAllowed(meta, filter);
        monitor.navigatorViewGenerationFailed(outputDirectory, viewResources, cause);
        monitor.navigatorViewNotGenerated();
        Properties properties = mock(Properties.class);
        monitor.processingSystemProperties(properties);
        ReportsCount count = mock(ReportsCount.class);
        monitor.reportsViewGenerated(count);
        monitor.reportsViewGenerationFailed(outputDirectory, formats, viewProperties, cause);
        monitor.reportsViewNotGenerated();
        monitor.runningEmbeddable(name);
        monitor.runningStory(storyPath);
        monitor.runningWithAnnotatedEmbedderRunner(name);
        List<String> storyPaths = asList("path1");
        monitor.storiesSkipped(storyPaths);
        monitor.storyFailed(storyPath, cause);
        Story story = mock(Story.class);
        long durationInSecs = 1L;
        long timeoutInSecs = 2L;
        StoryDuration storyDuration = new StoryDuration(timeoutInSecs);
        monitor.storyTimeout(story, storyDuration);
        String value = "value";
        monitor.systemPropertySet(name, value);
        int threads = 2;
        monitor.usingThreads(threads);
        
        // Then        
        verify(delegate).annotatedInstanceNotOfType(annotatedInstance, annotatedInstance.getClass());
        verify(delegate).batchFailed(failures);
        verify(delegate).beforeOrAfterStoriesFailed();
        verify(delegate).embeddableFailed(name, cause);
        verify(delegate).embeddableNotConfigurable(name);
        verify(delegate).embeddablesSkipped(names);
        verify(delegate).generatingMapsView(outputDirectory, storyMaps, viewProperties);
        verify(delegate).generatingNavigatorView(outputDirectory, viewResources);
        verify(delegate).generatingReportsView(outputDirectory, formats, viewProperties);
        verify(delegate).mappingStory(storyPath, metaFilters);
        verify(delegate).mapsViewGenerationFailed(outputDirectory, storyMaps, viewProperties, cause);
        verify(delegate).metaNotAllowed(meta, filter);
        verify(delegate).navigatorViewGenerationFailed(outputDirectory, viewResources, cause);
        verify(delegate).navigatorViewNotGenerated();
        verify(delegate).processingSystemProperties(properties);
        verify(delegate).reportsViewGenerated(count);
        verify(delegate).reportsViewGenerationFailed(outputDirectory, formats, viewProperties, cause);
        verify(delegate).reportsViewNotGenerated();
        verify(delegate).storiesSkipped(storyPaths);
        verify(delegate).storyFailed(storyPath, cause);
        verify(delegate).storyTimeout(story, storyDuration);
        verify(delegate).systemPropertySet(name, value);
        verify(delegate).usingThreads(threads);
   }

}
