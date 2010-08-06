package org.jbehave.mojo;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.Test;

public class EmbedderMojoBehaviour {

    private Embedder embedder = mock(Embedder.class);
    
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
