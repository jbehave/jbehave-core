package org.jbehave.core.embedder;

import java.io.PrintStream;

import org.jbehave.core.reporters.Format;

/**
 * Monitor that reports to a {@link PrintStream}, defaulting to {@link System#out}
 */
public class PrintStreamEmbedderMonitor extends PrintingEmbedderMonitor {

    private PrintStream output;

    public PrintStreamEmbedderMonitor() {
        this(System.out);
    }

    public PrintStreamEmbedderMonitor(PrintStream output) {
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
