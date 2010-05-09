package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.StoryEmbedder;

/**
 * Mojo to render reports
 * 
 * @goal render-reports
 */
public class ReportRendererMojo extends AbstractStoryMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        StoryEmbedder embedder = newStoryEmbedder();
        embedder.renderReports();
    }

}
