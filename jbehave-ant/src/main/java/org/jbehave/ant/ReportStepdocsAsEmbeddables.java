package org.jbehave.ant;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.steps.CandidateSteps;

import static org.apache.tools.ant.Project.MSG_INFO;

/**
 * Ant task to report stepdocs for the {@link Embeddable} instances provided (more specifically instances
 * of {@link ConfiguredEmbedder} which provides both {@link Configuration} and {@link CandidateSteps}).
 */
public class ReportStepdocsAsEmbeddables extends AbstractEmbedderTask {

	public void execute() throws BuildException {
		Embedder embedder = newEmbedder();
		log("Reporting stepdocs as embeddables using embedder " + embedder, MSG_INFO);
		embedder.reportStepdocsAsEmbeddables(classNames());
	}

}
