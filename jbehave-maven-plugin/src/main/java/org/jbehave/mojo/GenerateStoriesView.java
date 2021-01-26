package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to generate stories view
 */
@Mojo(name = "generate-stories-view", requiresDependencyResolution = ResolutionScope.TEST)
public class GenerateStoriesView extends AbstractEmbedderMojo {

    @Override
    public void execute() throws MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Generating stories view using embedder " + embedder);
        try {
            embedder.generateReportsView();
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to generate stories view", e);
        }
    }

}
