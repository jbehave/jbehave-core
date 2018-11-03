package org.jbehave.core.steps;

/**
 * StepMonitor that prints nothings.
 */
public class SilentStepMonitor extends PrintingStepMonitor {

    @Override
    protected void print(String format, Object... args) {
        // print nothing
    }
}
