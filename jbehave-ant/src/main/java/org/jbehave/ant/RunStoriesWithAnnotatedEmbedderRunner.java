package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runner.RunWith;

/**
 * Ant task that runs with {@link AnnotatedEmbedderRunner}, equivalent to
 * execution via JUnit's {@link RunWith}.
 */
public class RunStoriesWithAnnotatedEmbedderRunner extends AbstractEmbedderTask {

    public void execute() throws BuildException {
        Embedder embedder = newEmbedder();
        log("Running stories with annotated embedder " + annotatedEmbedderRunnerClass, MSG_INFO);
        embedder.runStoriesWithAnnotatedEmbedderRunner(annotatedEmbedderRunnerClass, classNames());
    }

}
