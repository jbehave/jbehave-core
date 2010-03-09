package org.jbehave.scenario.reporters;

import java.util.List;

import org.jbehave.scenario.steps.Stepdoc;

/**
 * Generates reports of generated StepDocs
 * 
 * @author Mauro Talevi
 */
public interface StepdocReporter {

	void report(List<Stepdoc> stepdocs);

}
