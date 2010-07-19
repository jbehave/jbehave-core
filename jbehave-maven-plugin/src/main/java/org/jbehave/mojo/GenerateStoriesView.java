package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to generate stories view
 * 
 * @goal generate-stories-view
 */
public class GenerateStoriesView extends AbstractEmbedderMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Generating stories view using embedder "+embedder);
        embedder.generateStoriesView();
    }

}
