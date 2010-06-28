package org.jbehave.ant;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.Embeddable;
import org.jbehave.core.junit.AnnotatedEmbedder;
import org.junit.runner.RunWith;

/**
 * Ant task that runs with {@link AnnotatedEmbedder}, equivalent to
 * execution via JUnit's {@link RunWith}.
 */
public class AnnotatedEmbedderRunnerTask extends AbstractStoryTask {

    public void execute() throws BuildException {
        for (AnnotatedEmbedder annotatedEmbedder : annotatedEmbedders()) {
            try {
                Object annotatedInstance = annotatedEmbedder.createTest();
                if ( annotatedInstance instanceof Embeddable ){
                    ((Embeddable)annotatedInstance).run();                    
                } else {
                    log(annotatedInstance+" not an "+Embeddable.class);
                }
            } catch (Throwable e) {
                throw new BuildException(annotatedEmbedder.toString(), e);
            }
        } 
    }

}
