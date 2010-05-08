package org.jbehave.ant;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.StoryEmbedder;

/**
 * Ant task that runs stories
 * 
 * @author Mauro Talevi
 */
public class StoryRunnerTask extends AbstractStoryTask {

    public void execute() throws BuildException {
        StoryEmbedder embedder = newStoryEmbedder();
        embedder.runStories(stories());
    }

}
