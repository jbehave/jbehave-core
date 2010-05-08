package org.jbehave.ant;

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
        embedder.renderReports(outputDirectory, formats, templateProperties);
    }
    
}
