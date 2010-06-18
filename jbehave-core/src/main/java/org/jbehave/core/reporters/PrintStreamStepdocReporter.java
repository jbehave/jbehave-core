package org.jbehave.core.reporters;

import static java.text.MessageFormat.format;

import java.io.PrintStream;
import java.util.List;

import org.jbehave.core.steps.Stepdoc;

public class PrintStreamStepdocReporter implements StepdocReporter {

	private static final String STEP_MATCHED_BY = "Step ''{0}'' is matched by annotated patterns:";
	private static final String STEP_NOT_MATCHED = "Step ''{0}'' is not matched by any pattern";
	private static final String STEPDOC = "''{0} {1}''";
	
	private PrintStream output;

	public PrintStreamStepdocReporter() {
		this(System.out);
	}

	public PrintStreamStepdocReporter(PrintStream output) {
		this.output = output;
	}

	public void stepdocsMatching(String stepAsString,
			List<Stepdoc> stepdocs, List<Object> stepsInstances) {
		if (stepdocs.size() > 0) {
			output(format(STEP_MATCHED_BY, stepAsString));
			outputStepdocs(stepdocs);
		} else {
			output(format(STEP_NOT_MATCHED, stepAsString));
		}
		outputStepsInstances(stepsInstances);
	}

	public void stepdocs(List<Stepdoc> stepdocs, List<Object> stepsInstances) {
		if (stepdocs.size() > 0) {
			outputStepdocs(stepdocs);
		} else {
			output("No stepdocs found");
		}
		outputStepsInstances(stepsInstances);
	}

	private void outputStepdocs(List<Stepdoc> stepdocs) {
		for (Stepdoc stepdoc : stepdocs) {
			output(format(STEPDOC, stepdoc.getStartingWord(), stepdoc.getPattern()));
			output(stepdoc.getMethodSignature());
		}
	}
	
	private void outputStepsInstances(List<Object> stepsInstances) {
		if (stepsInstances.size() > 0) {
			output("from steps instances:");
			for (Object stepsInstance : stepsInstances) {
				output(stepsInstance.getClass().getName());
			}
		} else {
			output("as no steps instances are provided");			
		}
	}
	
	private void output(Object object) {
		output.println(object);
	}

}
