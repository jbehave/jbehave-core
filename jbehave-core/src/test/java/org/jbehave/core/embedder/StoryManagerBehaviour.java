package org.jbehave.core.embedder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import org.codehaus.plexus.util.FileUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryManager.RunningStory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.junit.jupiter.api.Test;

class StoryManagerBehaviour {

    private PerformableTree performableTree = new PerformableTree();
    private EmbedderMonitor embedderMonitor = new NullEmbedderMonitor(); 
    private EmbedderControls embedderControls = new EmbedderControls();
    private ExecutorService executorService = mock(ExecutorService.class);
    private InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);

    @Test
    void shouldEnsureStoryReportOutputDirectoryExistsWhenWritingStoryDurations() throws IOException{
        Configuration configuration = new MostUsefulConfiguration();
        configuration.storyReporterBuilder().withRelativeDirectory("inexistent");
        File outputDirectory = configuration.storyReporterBuilder().outputDirectory();
        FileUtils.deleteDirectory(outputDirectory); 
        assertThat(outputDirectory.exists(), is(false));
        StoryManager manager = new StoryManager(configuration, stepsFactory, embedderControls, embedderMonitor, executorService, performableTree);
        Collection<RunningStory> runningStories = new ArrayList<>();
        manager.writeStoryDurations(runningStories);
        assertThat(outputDirectory.exists(), is(true));
    }

}
