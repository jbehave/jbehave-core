package org.jbehave.ant;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.StoryEmbedder;
import org.jbehave.core.StoryRunnerMode;

/**
 * Ant task that runs stories as paths
 *
 * @author Mauro Talevi
 */
public class StoryPathRunnerTask extends AbstractStoryTask {
    
    public void execute() throws BuildException {
        StoryEmbedder runner = newStoryEmbedder();
        runner.useRunnerMonitor(new AntRunnerMonitor());
        runner.useRunnerMode(new StoryRunnerMode(batch(), skipStories(), ignoreFailure()));
        runner.runStoriesAsPaths(storyPaths());
    }

}