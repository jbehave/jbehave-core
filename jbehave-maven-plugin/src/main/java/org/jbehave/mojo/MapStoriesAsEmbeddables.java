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

    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Mapping stories as embeddables using embedder "+embedder);
        embedder.runAsEmbeddables(classNames());
    }

}