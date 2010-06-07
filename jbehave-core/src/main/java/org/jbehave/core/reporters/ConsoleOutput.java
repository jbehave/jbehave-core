package org.jbehave.core.reporters;

import java.util.Properties;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Keywords;

/**
 * <p>
 * Story reporter that outputs as TXT to System.out.
 * </p>
 */
public class ConsoleOutput extends TxtOutput {

	public ConsoleOutput() {
		this(new LocalizedKeywords());
	}

	public ConsoleOutput(Properties outputPatterns) {
		this(outputPatterns, new LocalizedKeywords(), false);
	}

	public ConsoleOutput(Keywords keywords) {
		this(new Properties(), keywords, false);
	}

	public ConsoleOutput(Properties outputPatterns, Keywords keywords,
			boolean reportFailureTrace) {
		super(System.out, outputPatterns, keywords, reportFailureTrace);
	}

}
