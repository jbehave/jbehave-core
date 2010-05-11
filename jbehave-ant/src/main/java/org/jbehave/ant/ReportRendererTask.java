package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.StoryEmbedder;

/**
 * Ant task that renders reports
 * 
 * @author Mauro Talevi
 */
public class ReportRendererTask extends AbstractStoryTask {

    public void execute() throws BuildException {
        StoryEmbedder embedder = newStoryEmbedder();
		log("Rendering reports using embedder " + embedder, MSG_INFO);
        embedder.renderReports();
    }
    
}
