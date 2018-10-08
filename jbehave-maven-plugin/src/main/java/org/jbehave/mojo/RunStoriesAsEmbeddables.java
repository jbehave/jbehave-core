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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Running stories as embeddables using embedder " + embedder);
        try {
            embedder.runAsEmbeddables(classNames());
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to run stories as embeddables", e);
        }
    }

}
