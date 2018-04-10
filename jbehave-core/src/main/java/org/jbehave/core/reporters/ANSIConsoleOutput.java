package org.jbehave.core.reporters;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.jbehave.core.configuration.Keywords;

import static org.jbehave.core.reporters.ANSIConsoleOutput.SGRCode.*;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;

/**
 * <p>
 * Story reporter that outputs as ANSI-coded text to System.out.
 * </p>
 */
public class ANSIConsoleOutput extends ConsoleOutput {

    public enum SGRCode {
        RESET(0),
        BOLD(1),
        FAINT(2),
        ITALIC(3),
        UNDERLINE(4),
        SLOW_BLINK(5),
        RAPID_BLINK(6),
        NEGATIVE(7),
        CONCEALED(8),
        CROSSED_OUT(9),
        BLACK(30),
        RED(31),
        GREEN(32),
        YELLOW(33),
        BLUE(34),
        MAGENTA(35),
        CYAN(36),
        WHITE(37),
        ON_BLACK(40),
        ON_RED(41),
        ON_GREEN(42),
        ON_YELLOW(43),
        ON_BLUE(44),
        ON_MAGENTA(45),
        ON_CYAN(46),
        ON_WHITE(47),
        BRIGHT_BLACK(90),
        BRIGHT_RED(91),
        BRIGHT_GREEN(92),
        BRIGHT_YELLOW(93),
        BRIGHT_BLUE(94),
        BRIGHT_MAGENTA(95),
        BRIGHT_CYAN(96),
        BRIGHT_WHITE(97);

        private final int code;

        SGRCode(int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return Integer.toString(code);
        }
    }

    private static final char ESCAPE_CHARACTER = (char) 27;
    private static final String SGR_CONTROL = "m";
    private static final String CODE_SEPARATOR = ";";
    private SGRCode highlightCode = UNDERLINE;

    @SuppressWarnings("serial")
    private Map<String, SGRCode> codes = new HashMap<String, SGRCode>() {
        {
            put("narrative", BLUE);
            put("beforeScenario", CYAN);
            put("successful", GREEN);
            put("pending", YELLOW);
            put("pendingMethod", YELLOW);
            put("notPerformed", MAGENTA);
            put("comment", BLUE);
            put("ignorable", BLUE);
            put("failed", RED);
            put("cancelled", RED);
            put("restarted", MAGENTA);
        }
    };

    public ANSIConsoleOutput() {
        super();
    }

    public ANSIConsoleOutput(Keywords keywords) {
        super(keywords);
    }

    public ANSIConsoleOutput(Properties outputPatterns, Keywords keywords, boolean reportFailureTrace) {
        super(outputPatterns, keywords, reportFailureTrace);
    }

    public void assignCode(String key, SGRCode code) {
        codes.put(key, code);
    }

    public void withHighlightCode(SGRCode code) {
        this.highlightCode = code;
    }

    @Override
    protected String format(String key, String defaultPattern, Object... args) {
        String formatted = super.format(key, defaultPattern, args);

        if (codes.containsKey(key)) {
            SGRCode code = codes.get(key);
            formatted = escapeCodeFor(code) + highlightParameterValues(formatted, code) + escapeCodeFor(RESET);
        }

        return formatted;
    }

    private String highlightParameterValues(String formatted, SGRCode code) {
        final String valueStart = lookupPattern(PARAMETER_VALUE_START, PARAMETER_VALUE_START);
        final String valueEnd = lookupPattern(PARAMETER_VALUE_END, PARAMETER_VALUE_END);
        return formatted
                .replaceAll(valueStart, escapeCodeFor(highlightCode, code))
                .replaceAll(valueEnd, escapeCodeFor(RESET, code));
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
