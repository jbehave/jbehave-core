package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;


/**
 * Mojo to run stories as paths
 *
 * @author Mauro Talevi
 * @goal run-stories-as-paths
 */
public class StoryPathRunnerMojo extends AbstractEmbedderMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Running stories as paths using embedder "+embedder);
		embedder.runStoriesAsPaths(storyPaths());
    }

}