package org.jbehave.core.configuration;

import java.io.PrintStream;

import org.jbehave.core.reporters.Format;

/**
 * Monitor that reports to a {@link PrintStream}, defaulting to {@link System#out}
 */
public class PrintStreamAnnotationMonitor extends PrintingAnnotationMonitor {

    private final PrintStream output;

    public PrintStreamAnnotationMonitor() {
        this(System.out);
    }

    public PrintStreamAnnotationMonitor(PrintStream output) {
        this.output = output;
    }

    @Override
    protected void print(String format, Object... args) {
        Format.println(output, format, args);
    }

    @Override
    protected void printStackTrace(Throwable e) {
        e.printStackTrace(output);
    }
}
