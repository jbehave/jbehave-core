package org.jbehave.core.steps;

import java.io.PrintStream;

/**
 * StepMonitor that prints nothings.
 */
public class SilentStepMonitor extends PrintStreamStepMonitor {

    @Override
    protected void print(PrintStream output, String format, Object... args) {
        // print nothing
    }

}
