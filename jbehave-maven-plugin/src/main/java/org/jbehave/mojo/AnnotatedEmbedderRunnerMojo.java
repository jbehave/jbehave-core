package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.Embeddable;
import org.jbehave.core.junit.AnnotatedEmbedder;
import org.junit.runner.RunWith;

/**
 * Mojo that runs with {@link AnnotatedEmbedder}, equivalent to
 * execution via JUnit's {@link RunWith}.
 * 
 * @goal run-with-annotated-embedder
 */
public class AnnotatedEmbedderRunnerMojo extends AbstractStoryMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        for (AnnotatedEmbedder annotatedEmbedder : annotatedEmbedders()) {
            try {
                Object annotatedInstance = annotatedEmbedder.createTest();
                if ( annotatedInstance instanceof Embeddable ){
                    ((Embeddable)annotatedInstance).run();                    
                } else {
                    getLog().warn(annotatedInstance+" not an "+Embeddable.class);
                }
            } catch (Throwable e) {
                throw new MojoExecutionException(annotatedEmbedder.toString(), e);
            }
        } 
    }

}


