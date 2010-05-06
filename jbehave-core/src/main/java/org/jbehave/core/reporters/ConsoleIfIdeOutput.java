package org.jbehave.core.reporters;

import org.jbehave.core.model.Keywords;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class ConsoleIfIdeOutput extends TxtOutput {
    public ConsoleIfIdeOutput() {
        super(output());
    }

    public ConsoleIfIdeOutput(Properties outputPatterns) {
        super(output(), outputPatterns);
    }

    public ConsoleIfIdeOutput(Properties outputPatterns, Keywords keywords, boolean reportErrors) {
        super(output(), outputPatterns, keywords, reportErrors);
    }
    public static PrintStream output() {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (ste.getClassName().startsWith("com.intellij")) {
                return System.out;
            }
        }
        return new PrintStream(new ByteArrayOutputStream());

    }
}
