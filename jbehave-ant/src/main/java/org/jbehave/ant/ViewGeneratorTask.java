package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.embedder.Embedder;

/**
 * Ant task that generates a stories view
 */
public class ViewGeneratorTask extends AbstractEmbedderTask {

    public void execute() throws BuildException {
        Embedder embedder = newEmbedder();
		log("Generating stories view using embedder " + embedder, MSG_INFO);
        embedder.generateStoriesView();
    }
    
}
