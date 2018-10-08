package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to report stepdocs given a fully configured {@link Embedder} instance.
 * 
 * @goal report-stepdocs
 */
public class ReportStepdocs extends AbstractEmbedderMojo {

    @Override
    public void execute() throws MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Reporting stepdocs using embedder " + embedder);
        try {
            embedder.reportStepdocs();
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to report stepdocs", e);
        }
    }

}
