package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.embedder.Embedder;

/**
 * Ant task that runs stories as paths
 */
public class RunStoriesAsPaths extends AbstractEmbedderTask {
    
    public void execute() throws BuildException {
        Embedder embedder = newEmbedder();
        log("Running stories as paths using embedder "+embedder, MSG_INFO);
		embedder.runStoriesAsPaths(storyPaths());
    }

}