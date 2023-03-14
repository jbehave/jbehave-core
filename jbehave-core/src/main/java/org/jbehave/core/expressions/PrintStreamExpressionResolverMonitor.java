package org.jbehave.core.expressions;

import java.io.PrintStream;

import org.jbehave.core.reporters.Format;

/**
 * Monitor that reports to a {@link PrintStream}, defaulting to {@link System#out}
 */
public class PrintStreamExpressionResolverMonitor extends PrintingExpressionResolverMonitor {

    private PrintStream output;

    public PrintStreamExpressionResolverMonitor() {
        this(System.out);
    }

    public PrintStreamExpressionResolverMonitor(PrintStream output) {
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
