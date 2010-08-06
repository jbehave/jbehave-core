package org.jbehave.mojo;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.Test;

public class EmbedderMojoBehaviour {

    private Embedder embedder = mock(Embedder.class);
    
    @Test
    public void shouldCreateNewEmbedderWithDefaultControls(){
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            public void execute() throws MojoExecutionException, MojoFailureException {
            }
        };
        // When
        Embedder embedder = mojo.newEmbedder();
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
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            public void execute() throws MojoExecutionException, MojoFailureException {
            }
        };
        // When
        mojo.batch = true;
        mojo.generateViewAfterStories = false;
        mojo.ignoreFailureInStories = true;
        mojo.ignoreFailureInView = true;
        mojo.skip = true;
        Embedder embedder = mojo.newEmbedder();
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
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            public void execute() throws MojoExecutionException, MojoFailureException {
            }
        };
        // When
        Embedder embedder = mojo.newEmbedder();
        // Then
        EmbedderMonitor embedderMonitor = embedder.embedderMonitor();
        assertThat(embedderMonitor.toString(), containsString("MavenEmbedderMonitor"));
    }
    
    @Test
    public void shouldGenerateStoriesView() throws MojoExecutionException, MojoFailureException{
        // Given
        GenerateStoriesView mojo = new GenerateStoriesView(){
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }
            
        };
        // When
        mojo.execute();
        
        // Then 
        verify(embedder).generateStoriesView();
    }


    @Test
    public void shouldReportStepdocs() throws MojoExecutionException, MojoFailureException{
        // Given
        ReportStepdocs mojo = new ReportStepdocs(){
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }            
            
        };
        // When
        mojo.execute();
        
        // Then 
        verify(embedder).reportStepdocs();
    }
    
    @Test
    public void shouldRunStoriesAsEmbeddables() throws MojoExecutionException, MojoFailureException{
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsEmbeddables mojo = new RunStoriesAsEmbeddables(){
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/stories/*.java");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);
        
        // When
        mojo.execute();
        
        // Then 
        verify(embedder).runStoriesAsEmbeddables(classNames, classLoader);
    }
    
    @Test
    public void shouldRunStoriesAsPaths() throws MojoExecutionException, MojoFailureException{
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsPaths mojo = new RunStoriesAsPaths(){
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/stories/*.story");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> storyPaths = new StoryFinder().findPaths(searchInDirectory, includes, excludes);
        
        // When
        mojo.execute();
        
        // Then 
        verify(embedder).runStoriesAsPaths(storyPaths);
    }
    
    @Test
    public void shouldRunStoriesWithAnnotatedEmbedderRunner() throws MojoExecutionException, MojoFailureException{
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesWithAnnotatedEmbedderRunner mojo = new RunStoriesWithAnnotatedEmbedderRunner(){
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
        mojo.annotatedEmbedderRunnerClass = runnerClass;
        String searchInDirectory = "src/test/java/";
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/stories/*.java");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);
        
        // When
        mojo.execute();
        
        // Then 
        verify(embedder).runStoriesWithAnnotatedEmbedderRunner(runnerClass, classNames, classLoader);
    }
    
}
