package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.embedder.Embedder;

/**
 * Ant task to report stepdocs given a fully configured {@link Embedder} instance.
 */
public class ReportStepdocs extends AbstractEmbedderTask {

	@Override
    public void execute() throws BuildException {
		Embedder embedder = newEmbedder();
		log("Reporting stepdocs using embedder " + embedder, MSG_INFO);
		embedder.reportStepdocs();
	}

}
