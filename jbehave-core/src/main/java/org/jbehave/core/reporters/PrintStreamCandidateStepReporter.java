package org.jbehave.core.reporters;

import static java.text.MessageFormat.format;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jbehave.core.steps.CandidateStep;

public class PrintStreamCandidateStepReporter implements CandidateStepReporter {

	private static final String STEP_MATCHED_BY = "Step ''{0}'' is matched by annotated methods:";
	private static final String STEP_NOT_MATCHED = "Step ''{0}'' is not matched by any method";

	private PrintStream output;

	public PrintStreamCandidateStepReporter() {
		this(System.out);
	}

	public PrintStreamCandidateStepReporter(PrintStream output) {
		this.output = output;
	}

	public void candidateStepsMatching(String stepAsString,
			List<CandidateStep> candidateSteps, List<Object> stepsInstances) {
		if (candidateSteps.size() > 0) {
			output.println(format(STEP_MATCHED_BY, stepAsString));
			for (CandidateStep candidateStep : candidateSteps) {
				Method method = candidateStep.getMethod();
				for (Annotation annotation : method.getAnnotations()) {
					output.println(annotation);
				}
				output.println(method);
			}
		} else {
			output.println(format(STEP_NOT_MATCHED, stepAsString));
		}
		if (stepsInstances.size() > 0) {
			output.println("from steps instances:");
			for (Object stepsInstance : stepsInstances) {
				output.println(stepsInstance.getClass().getName());
			}
		} else {
			output.println("as no steps instances are provided");			
		}
	}

}
