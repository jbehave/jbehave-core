package org.jbehave.ant;

import static org.apache.tools.ant.Project.MSG_INFO;

import org.apache.tools.ant.BuildException;
import org.jbehave.core.embedder.Embedder;

/**
 * Ant task to report stepdocs
 */
public class ReportStepdocsAsEmbeddables extends AbstractEmbedderTask {

	public void execute() throws BuildException {
		Embedder embedder = newEmbedder();
		log("Reporting stepdocs as embeddables using embedder " + embedder, MSG_INFO);
		embedder.reportStepdocsAsEmbeddables(classNames());
	}

}
