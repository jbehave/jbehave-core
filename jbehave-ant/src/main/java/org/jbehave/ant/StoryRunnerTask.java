package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import java.util.List;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.RunnableStory;
import org.jbehave.core.embedder.Embedder;

/**
 * Ant task that runs stories
 */
public class StoryRunnerTask extends AbstractStoryTask {

    public void execute() throws BuildException {
        Embedder embedder = newEmbedder();
        List<RunnableStory> stories = runnableStories();
		log("Running stories using embedder "+embedder, MSG_INFO);
		embedder.runStories(stories);
    }

}
