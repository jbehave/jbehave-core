package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.StoryEmbedder;


/**
 * Mojo to run stories via paths
 *
 * @author Mauro Talevi
 * @goal run-stories-as-paths
 */
public class StoryPathRunnerMojo extends AbstractStoryMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        StoryEmbedder embedder = newStoryEmbedder();
        embedder.runStoriesAsPaths(storyPaths());
    }

}