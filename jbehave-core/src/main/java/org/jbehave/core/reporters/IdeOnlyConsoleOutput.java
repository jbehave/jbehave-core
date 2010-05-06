package org.jbehave.core.reporters;

import org.jbehave.core.model.Keywords;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * Outputs to the console only if running in an IDE.  Command line builds (Maven, Ant)
 * will produce nothing for this particular PrintStreamOutput specialisation
 */
public class IdeOnlyConsoleOutput extends TxtOutput {
    public IdeOnlyConsoleOutput() {
        super(output());
    }

    public IdeOnlyConsoleOutput(Properties outputPatterns) {
        super(output(), outputPatterns);
    }

    public IdeOnlyConsoleOutput(Properties outputPatterns, Keywords keywords, boolean reportErrors) {
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
