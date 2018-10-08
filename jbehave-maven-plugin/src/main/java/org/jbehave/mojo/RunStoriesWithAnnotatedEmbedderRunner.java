package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runner.RunWith;

/**
 * Mojo that runs stories with {@link AnnotatedEmbedderRunner}, equivalent to
 * execution via JUnit's {@link RunWith}.
 * 
 * @goal run-stories-with-annotated-embedder
 */
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
