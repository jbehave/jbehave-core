package org.jbehave.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to report stepdocs
 * 
 * @goal report-stepdocs-as-embeddables
 */
public class ReportStepdocsAsEmbeddables extends AbstractEmbedderMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Reporting stepdocs as embeddables using embedder "+embedder);
        embedder.reportStepdocsAsEmbeddables(classNames());
    }

}
