package org.jbehave.core.reporters;

import java.io.PrintStream;
import java.util.List;
import org.jbehave.core.steps.Stepdoc;

public class PrintStreamStepdocReporter implements StepdocReporter {

    private static final String STEP_MATCHED_BY = "Step '%s' is matched by annotated patterns:";
    private static final String STEP_NOT_MATCHED = "Step '%s' is not matched by any pattern";
    private static final String STEPDOC = "'%s %s'";

    private PrintStream output;

    public PrintStreamStepdocReporter() {
        this(System.out);
    }

    public PrintStreamStepdocReporter(PrintStream output) {
        this.output = output;
    }

    @Override
    public void stepdocsMatching(String stepAsString,
            List<Stepdoc> stepdocs, List<Object> stepsInstances) {
        if (stepdocs.size() > 0) {
            output(STEP_MATCHED_BY, stepAsString);
            outputStepdocs(stepdocs);
        } else {
            output(STEP_NOT_MATCHED, stepAsString);
        }
        outputStepsInstances(stepsInstances);
    }

    @Override
    public void stepdocs(List<Stepdoc> stepdocs, List<Object> stepsInstances) {
        outputStepdocs(stepdocs);
        outputStepsInstances(stepsInstances);
    }

    private void outputStepdocs(List<Stepdoc> stepdocs) {
        for (Stepdoc stepdoc : stepdocs) {
            output(STEPDOC, stepdoc.getStartingWord(), stepdoc.getPattern());
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

    private void output(String format, Object... args) {
        Format.println(output, format, args);
    }

}
