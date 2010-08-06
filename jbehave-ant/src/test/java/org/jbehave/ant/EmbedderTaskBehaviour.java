package org.jbehave.ant;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.Test;

public class EmbedderTaskBehaviour {

    private Embedder embedder = mock(Embedder.class);

    @Test
    public void shouldCreateNewEmbedderWithDefaultControls(){
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        Embedder embedder = task.newEmbedder();
        // Then
        EmbedderControls embedderControls = embedder.embedderControls();
        assertThat(embedderControls.batch(), is(false));
        assertThat(embedderControls.generateViewAfterStories(), is(true));
        assertThat(embedderControls.ignoreFailureInStories(), is(false));
        assertThat(embedderControls.ignoreFailureInView(), is(false));
        assertThat(embedderControls.skip(), is(false));        
    }

    @Test
    public void shouldCreateNewEmbedderWithGivenControls(){
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        task.setBatch(true);
        task.setGenerateViewAfterStories(false);
        task.setIgnoreFailureInStories(true);
        task.setIgnoreFailureInView(true);
        task.setSkip(true);
        Embedder embedder = task.newEmbedder();
        // Then
        EmbedderControls embedderControls = embedder.embedderControls();
        assertThat(embedderControls.batch(), is(true));
        assertThat(embedderControls.generateViewAfterStories(), is(false));
        assertThat(embedderControls.ignoreFailureInStories(), is(true));
        assertThat(embedderControls.ignoreFailureInView(), is(true));
        assertThat(embedderControls.skip(), is(true));        
    }

    @Test
    public void shouldCreateNewEmbedderWithAntMonitor(){
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        Embedder embedder = task.newEmbedder();
        // Then
        EmbedderMonitor embedderMonitor = embedder.embedderMonitor();
        assertThat(embedderMonitor.toString(), containsString("AntEmbedderMonitor"));
    }

    @Test
    public void shouldGenerateStoriesView(){
        // Given
        GenerateStoriesView task = new GenerateStoriesView(){
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }
            
        };
        // When
        task.execute();
        
        // Then 
        verify(embedder).generateStoriesView();
    }


    @Test
    public void shouldReportStepdocs(){
        // Given
        ReportStepdocs task = new ReportStepdocs(){
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }            
            
        };
        // When
        task.execute();
        
        // Then 
        verify(embedder).reportStepdocs();
    }
    
    @Test
    public void shouldRunStoriesAsEmbeddables(){
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsEmbeddables task = new RunStoriesAsEmbeddables(){
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }
            
            @Override
            protected EmbedderClassLoader createClassLoader() {
                return classLoader;
            }

        };
        String searchInDirectory = "src/test/java/";
        task.setSourceDirectory(searchInDirectory);
        List<String> includes = asList("**/stories/*.java");
        task.setIncludes(StringUtils.join(includes, "'"));
        List<String> excludes = asList();
        task.setExcludes(StringUtils.join(excludes, "'"));
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);
        
        // When
        task.execute();
        
        // Then 
        verify(embedder).runStoriesAsEmbeddables(classNames, classLoader);
    }
    
    @Test
    public void shouldRunStoriesAsPaths(){
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsPaths task = new RunStoriesAsPaths(){
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }
            
            @Override
            protected EmbedderClassLoader createClassLoader() {
                return classLoader;
            }

        };
        String searchInDirectory = "src/test/java/";
        task.setSourceDirectory(searchInDirectory);
        List<String> includes = asList("**/stories/*.story");
        task.setIncludes(StringUtils.join(includes, "'"));
        List<String> excludes = asList();
        task.setExcludes(StringUtils.join(excludes, "'"));
        List<String> storyPaths = new StoryFinder().findPaths(searchInDirectory, includes, excludes);
        
        // When
        task.execute();
        
        // Then 
        verify(embedder).runStoriesAsPaths(storyPaths);
    }
    
    @Test
    public void shouldRunStoriesWithAnnotatedEmbedderRunner(){
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesWithAnnotatedEmbedderRunner task = new RunStoriesWithAnnotatedEmbedderRunner(){
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }
            
            @Override
            protected EmbedderClassLoader createClassLoader() {
                return classLoader;
            }

        };
        String runnerClass = AnnotatedEmbedderRunner.class.getName();
        task.setAnnotatedEmbedderRunnerClass(runnerClass);
        String searchInDirectory = "src/test/java/";
        task.setSourceDirectory(searchInDirectory);
        List<String> includes = asList("**/stories/*.java");
        task.setIncludes(StringUtils.join(includes, "'"));
        List<String> excludes = asList();
        task.setExcludes(StringUtils.join(excludes, "'"));
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);
        
        // When
        task.execute();
        
        // Then 
        verify(embedder).runStoriesWithAnnotatedEmbedderRunner(runnerClass, classNames, classLoader);
    }
    
}
