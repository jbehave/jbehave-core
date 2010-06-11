package org.jbehave.core.reporters;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;

/**
 * Outputs to the console only if running in an IDE. Command line builds (Maven,
 * Ant) will produce nothing for this particular PrintStreamOutput
 * specialisation.
 */
public class IdeOnlyConsoleOutput extends TxtOutput {
	
	public IdeOnlyConsoleOutput() {
		super(output());
	}

	public IdeOnlyConsoleOutput(Properties outputPatterns) {
		super(output(), outputPatterns);
	}

	public IdeOnlyConsoleOutput(Properties outputPatterns, Keywords keywords,
			boolean reportErrors) {
		super(output(), outputPatterns, keywords, reportErrors);
	}

	public static PrintStream output() {
		if (inIDE()) {
			return System.out;
		}
		return new PrintStream(new ByteArrayOutputStream());
	}

	private static boolean inIDE() {
		List<String> idePackages = Arrays.asList("com.intellij", "org.eclipse");
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			for (String idePackage : idePackages) {
				if (ste.getClassName().startsWith(idePackage)) {
					return true;
				}
			}
		}
		return false;
	}
}
