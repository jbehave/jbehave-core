package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runner.RunWith;

/**
 * Mojo that runs with {@link AnnotatedEmbedderRunner}, equivalent to
 * execution via JUnit's {@link RunWith}.
 * 
 * @goal run-with-annotated-embedder
 */
public class RunStoriesWithAnnotatedEmbedderRunner extends AbstractEmbedderMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Running stories with annotated embedder "+annotatedEmbedderRunnerClass);
        embedder.runStoriesWithAnnotatedEmbedderRunner(annotatedEmbedderRunnerClass, classNames(), createClassLoader());
    }

}

