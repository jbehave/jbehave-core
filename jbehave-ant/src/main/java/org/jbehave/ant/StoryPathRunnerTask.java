package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import java.util.List;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.embedder.Embedder;

/**
 * Ant task that runs stories as paths
 *
 * @author Mauro Talevi
 */
public class StoryPathRunnerTask extends AbstractStoryTask {
    
    public void execute() throws BuildException {
        Embedder embedder = newEmbedder();
        List<String> storyPaths = storyPaths();
		log("Running stories with paths "+storyPaths+" using embedder "+embedder, MSG_INFO);
		embedder.runStoriesAsPaths(storyPaths);
    }

}