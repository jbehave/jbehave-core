package org.jbehave.mojo;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;


/**
 * Mojo to run stories via paths
 *
 * @author Mauro Talevi
 * @goal run-stories-as-paths
 */
public class StoryPathRunnerMojo extends AbstractStoryMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        List<String> storyPaths = storyPaths();
        getLog().info("Running stories with paths "+storyPaths+" using embedder "+embedder);
		embedder.runStoriesAsPaths(storyPaths);
    }

}