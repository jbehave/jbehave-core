package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to run stories as Embeddables
 *
 * @goal run-stories-as-embeddables
 */
public class RunStoriesAsEmbeddables extends AbstractEmbedderMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Running stories using embedder "+embedder);
		embedder.runAsEmbeddables(classNames());
    }

}


