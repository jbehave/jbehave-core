package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.embedder.Embedder;

/**
 * Ant task that maps stories as embeddables
 */
public class MapStoriesAsEmbeddables extends AbstractEmbedderTask {
    
    public void execute() throws BuildException {
        Embedder embedder = newEmbedder();
        log("Mapping stories as embeddables using embedder "+embedder, MSG_INFO);
		embedder.runAsEmbeddables(classNames());
    }

}