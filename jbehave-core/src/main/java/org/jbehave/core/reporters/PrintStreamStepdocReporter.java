package org.jbehave.core.reporters;

import java.io.PrintStream;

public class PrintStreamStepdocReporter extends PrintingStepdocReporter {

    private PrintStream output;

    public PrintStreamStepdocReporter() {
        this(System.out);
    }

    public PrintStreamStepdocReporter(PrintStream output) {
        this.output = output;
    }

    @Override
    protected void output(String format, Object... args) {
        Format.println(output, format, args);
    }
}
