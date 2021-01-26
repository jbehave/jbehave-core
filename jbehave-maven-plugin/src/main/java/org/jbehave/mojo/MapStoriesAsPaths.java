package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to map stories as paths
 */
@Mojo(name = "map-stories-as-paths", requiresDependencyResolution = ResolutionScope.TEST)
public class MapStoriesAsPaths extends AbstractEmbedderMojo {

    @Override
    public void execute() throws MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Mapping stories as paths using embedder " + embedder);
        try {
            embedder.mapStoriesAsPaths(storyPaths());
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to map stories as paths", e);
        }
    }

}
