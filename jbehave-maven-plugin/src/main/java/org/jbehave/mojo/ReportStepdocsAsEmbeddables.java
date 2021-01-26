package org.jbehave.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.steps.CandidateSteps;

/**
 * Mojo to report stepdocs for the {@link Embeddable} instances provided (more
 * specifically instances of {@link ConfiguredEmbedder} which provides both
 * {@link Configuration} and {@link CandidateSteps}).
 */
@Mojo(name = "report-stepdocs-as-embeddables", requiresDependencyResolution = ResolutionScope.TEST)
public class ReportStepdocsAsEmbeddables extends AbstractEmbedderMojo {

    @Override
    public void execute() throws MojoFailureException {
        Embedder embedder = newEmbedder();
        getLog().info("Reporting stepdocs as embeddables using embedder " + embedder);
        try {
            embedder.reportStepdocsAsEmbeddables(classNames());
        } catch (RuntimeException e) {
            throw new MojoFailureException("Failed to report stepdocs as embeddables", e);
        }
    }

}
