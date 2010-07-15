package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.embedder.Embedder;

/**
 * Ant task that runs stories
 */
public class StoryRunnerTask extends AbstractEmbedderTask {

    public void execute() throws BuildException {
        Embedder embedder = newEmbedder();
		log("Running stories using embedder "+embedder, MSG_INFO);
        embedder.runStoriesAsEmbeddables(classNames(), createClassLoader());
    }

}
