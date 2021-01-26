package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jbehave.core.embedder.Embedder;

/**
 * Mojo to report stepdocs given a fully configured {@link Embedder} instance.
 */
@Mojo(name = "report-stepdocs", requiresDependencyResolution = ResolutionScope.TEST)
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
