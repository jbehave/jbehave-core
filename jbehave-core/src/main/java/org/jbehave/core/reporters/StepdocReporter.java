package org.jbehave.core.reporters;

import java.util.List;

import org.jbehave.core.steps.Stepdoc;

/**
 * Generates reports of generated StepDocs
 * 
 * @author Mauro Talevi
 */
public interface StepdocReporter {

	void report(List<Stepdoc> stepdocs);

}
