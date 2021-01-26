package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to run stories as paths
 */
@Mojo(name = "run-stories-as-paths", requiresDependencyResolution = ResolutionScope.TEST)
public class RunStoriesAsPaths extends AbstractEmbedderMojo {

    @Override
    public void execute() throws MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Running stories as paths using embedder " + embedder);
        try {
            embedder.runStoriesAsPaths(storyPaths());
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to run stories as paths", e);
        }
    }

}
