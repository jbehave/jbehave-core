package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;

/**
 * Mojo that runs stories with {@link AnnotatedEmbedderRunner}.
 */
@Mojo(name = "run-stories-with-annotated-embedder", requiresDependencyResolution = ResolutionScope.TEST)
public class RunStoriesWithAnnotatedEmbedderRunner extends AbstractEmbedderMojo {

    @Override
    public void execute() throws MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Running stories with annotated embedder runner");
        try {
            embedder.runStoriesWithAnnotatedEmbedderRunner(classNames());
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to run stories with annotated embedder runner", e);
        }

    }

}
