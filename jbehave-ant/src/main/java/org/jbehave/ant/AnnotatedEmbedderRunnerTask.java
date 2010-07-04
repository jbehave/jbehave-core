package org.jbehave.ant;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.Embeddable;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runner.RunWith;

/**
 * Ant task that runs with {@link AnnotatedEmbedderRunner}, equivalent to
 * execution via JUnit's {@link RunWith}.
 */
public class AnnotatedEmbedderRunnerTask extends AbstractEmbedderTask {

    public void execute() throws BuildException {
        for (AnnotatedEmbedderRunner annotatedEmbedderRunner : annotatedEmbedderRunners()) {
            try {
                Object annotatedInstance = annotatedEmbedderRunner.createTest();
                if ( annotatedInstance instanceof Embeddable ){
                    ((Embeddable)annotatedInstance).run();                    
                } else {
                    log(annotatedInstance+" not an "+Embeddable.class);
                }
            } catch (Throwable e) {
                throw new BuildException(annotatedEmbedderRunner.toString(), e);
            }
        } 
    }

}
