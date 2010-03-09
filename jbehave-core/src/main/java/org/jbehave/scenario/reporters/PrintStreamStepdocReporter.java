package org.jbehave.scenario.reporters;

import static java.text.MessageFormat.format;

import java.io.PrintStream;
import java.util.List;

import org.jbehave.scenario.steps.Stepdoc;

/**
 * <p>
 * Stepdoc reporter that outputs to a PrintStream, defaulting to System.out.
 * </p>
 */
public class PrintStreamStepdocReporter implements StepdocReporter {

	private static final String STEP = "Step: {0} {1}";
	private static final String ALIASES = "Aliases: {0}";
	private static final String METHOD = "Method: {0}";
	private final PrintStream output;
	private final boolean reportMethods;

	public PrintStreamStepdocReporter() {
		this(System.out);
	}

	public PrintStreamStepdocReporter(boolean reportMethods) {
		this(System.out, reportMethods);
	}

	public PrintStreamStepdocReporter(PrintStream output) {
		this(output, false);
	}

	public PrintStreamStepdocReporter(PrintStream output, boolean reportMethods) {
		this.output = output;
		this.reportMethods = reportMethods;
	}

	public void report(List<Stepdoc> stepdocs) {
		for (Stepdoc stepdoc : stepdocs) {
			output.println(format(STEP, stepdoc.getAnnotation().getSimpleName(), stepdoc.getPattern()));
			if (stepdoc.getAliasPatterns().size() > 0) {
				output.println(format(ALIASES, stepdoc.getAliasPatterns()));
			}
			if (reportMethods) {
				output.println(format(METHOD, stepdoc.getMethod()));
			}
		}
	}

}
