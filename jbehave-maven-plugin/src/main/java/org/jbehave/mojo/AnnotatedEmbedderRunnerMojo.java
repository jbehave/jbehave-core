package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.Embeddable;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runner.RunWith;

/**
 * Mojo that runs with {@link AnnotatedEmbedderRunner}, equivalent to
 * execution via JUnit's {@link RunWith}.
 * 
 * @goal run-with-annotated-embedder
 */
public class AnnotatedEmbedderRunnerMojo extends AbstractEmbedderMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        for (AnnotatedEmbedderRunner annotatedEmbedderRunner : annotatedEmbedderRunners()) {
            try {
                Object annotatedInstance = annotatedEmbedderRunner.createTest();
                if ( annotatedInstance instanceof Embeddable ){
                    ((Embeddable)annotatedInstance).run();                    
                } else {
                    getLog().warn(annotatedInstance+" not an "+Embeddable.class);
                }
            } catch (Throwable e) {
                throw new MojoExecutionException(annotatedEmbedderRunner.toString(), e);
            }
        } 
    }

}


