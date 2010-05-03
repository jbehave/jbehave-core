package org.jbehave.core.reporters;

import java.util.Properties;

import org.jbehave.core.model.Keywords;

/**
 * <p>
 * Story reporter that outputs as TXT to System.out.
 * </p>
 */
public class ConsoleOutput extends TxtOutput {

	public ConsoleOutput() {
		super(System.out);
	}

	public ConsoleOutput(Properties outputPatterns) {
		super(System.out, outputPatterns);
	}

	public ConsoleOutput(Properties outputPatterns, Keywords keywords,
			boolean reportErrors) {
		super(System.out, outputPatterns, keywords, reportErrors);
	}

}
