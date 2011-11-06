package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to map stories as paths
 * 
 * @goal map-stories-as-paths
 */
public class MapStoriesAsPaths extends AbstractEmbedderMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Mapping stories as paths using embedder " + embedder);
        try {
            embedder.mapStoriesAsPaths(storyPaths());
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to map stories as paths", e);
        }
    }

}