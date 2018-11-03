package org.jbehave.core.steps;

import java.io.PrintStream;

import org.jbehave.core.reporters.Format;

/**
 * StepMonitor that prints to a {@link PrintStream}, defaulting to {@link System#out}.
 */
public class PrintStreamStepMonitor extends PrintingStepMonitor {

    private final PrintStream output;

    public PrintStreamStepMonitor() {
        this(System.out);
    }

    public PrintStreamStepMonitor(PrintStream output) {
        this.output = output;
    }

    @Override
    protected void print(String format, Object... args) {
        Format.println(output, format, args);
    }
}
