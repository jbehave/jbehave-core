package org.jbehave.core.reporters;

import static org.jbehave.core.reporters.SGRCodes.SGRCode.RESET;
import static org.jbehave.core.reporters.SGRCodes.SGRCode.UNDERLINE;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;

import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.reporters.SGRCodes.SGRCode;

/**
 * <p>
 * Story reporter that outputs as ANSI-coded text to System.out.
 * Uses {@link SGRCodes} to decorate ConsoleOutput format patterns.
 * </p>
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class ANSIConsoleOutput extends ConsoleOutput {

    private static final char ESCAPE_CHARACTER = (char) 27;
    private static final String SGR_CONTROL = "m";
    private static final String CODE_SEPARATOR = ";";

    private SGRCodes codes = new SGRCodes();

    public ANSIConsoleOutput() {
        super();
    }

    public ANSIConsoleOutput(Keywords keywords) {
        super(keywords);
    }

    public ANSIConsoleOutput(Properties outputPatterns, Keywords keywords, boolean reportFailureTrace) {
        super(outputPatterns, keywords, reportFailureTrace);
    }

    public ANSIConsoleOutput assignCode(String key, SGRCode code) {
        codes.assignCode(key, code);
        return this;
    }

    public ANSIConsoleOutput withCodes(SGRCodes codes) {
        this.codes = codes;
        return this;
    }

    @Override
    protected String format(String key, String defaultPattern, Object... args) {
        String formatted = super.format(key, defaultPattern, args);

        if (codes.hasCode(key)) {
            SGRCode code = codes.getCode(key);
            formatted = escapeCodeFor(code) + highlightParameterValues(formatted, code) + escapeCodeFor(RESET);
        }

        return formatted;
    }

    private String highlightParameterValues(String formatted, SGRCode code) {
        final String valueStart = lookupPattern(PARAMETER_VALUE_START, PARAMETER_VALUE_START);
        final String valueEnd = lookupPattern(PARAMETER_VALUE_END, PARAMETER_VALUE_END);
        return formatted
                .replaceAll(valueStart, escapeCodeFor(highlightCode(), code))
                .replaceAll(valueEnd, escapeCodeFor(RESET, code));
    }

    private SGRCode highlightCode() {
        if (codes.hasCode("highlight")) {
            return codes.getCode("highlight");
        }
        return UNDERLINE;
    }

    private String escapeCodeFor(SGRCode code) {
        return controlSequenceInitiator(code + SGR_CONTROL);
    }

    private String escapeCodeFor(SGRCode first, SGRCode second) {
        return controlSequenceInitiator(first + CODE_SEPARATOR + second + SGR_CONTROL);
    }

    private String controlSequenceInitiator(String code) {
        return ESCAPE_CHARACTER + "[" + code;
    }

}
