package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to map stories as embeddables
 */
@Mojo(name = "map-stories-as-embeddables", requiresDependencyResolution = ResolutionScope.TEST)
public class MapStoriesAsEmbeddables extends AbstractEmbedderMojo {

    @Override
    public void execute() throws MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Mapping stories as embeddables using embedder " + embedder);
        try {
            embedder.runAsEmbeddables(classNames());
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to map stories as embeddables", e);
        }
    }

}
