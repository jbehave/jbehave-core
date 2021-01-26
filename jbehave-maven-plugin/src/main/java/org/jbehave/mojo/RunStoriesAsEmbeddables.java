package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to run stories as Embeddables
 */
@Mojo(name = "run-stories-as-embeddables", requiresDependencyResolution = ResolutionScope.TEST)
public class RunStoriesAsEmbeddables extends AbstractEmbedderMojo {

    @Override
    public void execute() throws MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Running stories as embeddables using embedder " + embedder);
        try {
            embedder.runAsEmbeddables(classNames());
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to run stories as embeddables", e);
        }
    }

}
