package org.jbehave.ant;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.StoryEmbedder;

/**
 * Ant task that runs stories as paths
 *
 * @author Mauro Talevi
 */
public class StoryPathRunnerTask extends AbstractStoryTask {
    
    public void execute() throws BuildException {
        StoryEmbedder runner = newStoryEmbedder();
        runner.runStoriesAsPaths(storyPaths());
    }

}