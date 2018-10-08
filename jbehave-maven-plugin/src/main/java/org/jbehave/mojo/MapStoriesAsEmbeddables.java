package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to map stories as embeddables
 * 
 * @goal map-stories-as-embeddables
 */
public class MapStoriesAsEmbeddables extends AbstractEmbedderMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Mapping stories as embeddables using embedder " + embedder);
        try {
            embedder.runAsEmbeddables(classNames());
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to map stories as embeddables", e);
        }
    }

}
