package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.StoryEmbedder;

/**
 * Mojo to run stories
 *
 * @author Mauro Talevi
 * @goal run-stories
 */
public class StoryRunnerMojo extends AbstractStoryMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        StoryEmbedder embedder = newStoryEmbedder();
        embedder.runStories(stories());
    }

}


