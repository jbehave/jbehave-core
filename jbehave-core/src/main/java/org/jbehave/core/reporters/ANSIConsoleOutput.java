package org.jbehave.core.reporters;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;

import static org.jbehave.core.reporters.ANSIConsoleOutput.SGRCode.BLUE;
import static org.jbehave.core.reporters.ANSIConsoleOutput.SGRCode.BOLD;
import static org.jbehave.core.reporters.ANSIConsoleOutput.SGRCode.GREEN;
import static org.jbehave.core.reporters.ANSIConsoleOutput.SGRCode.MAGENTA;
import static org.jbehave.core.reporters.ANSIConsoleOutput.SGRCode.RED;
import static org.jbehave.core.reporters.ANSIConsoleOutput.SGRCode.RESET;
import static org.jbehave.core.reporters.ANSIConsoleOutput.SGRCode.YELLOW;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;

/**
 * <p>
 * Story reporter that outputs as ANSI-coded text to System.out.
 * </p>
 */
public class ANSIConsoleOutput extends ConsoleOutput {

    private static final char ESCAPE_CHARACTER = (char) 27;
    private static final String SGR_CONTROL = "m";
    private static final String CODE_SEPARATOR = ";";

    @SuppressWarnings("serial")
    private Map<String, SGRCode> codes = new HashMap<String, SGRCode>() {
        {
            put("successful", GREEN);
            put("pending", YELLOW);
            put("pendingMethod", YELLOW);
            put("notPerformed", MAGENTA);
            put("ignorable", BLUE);
            put("failed", RED);
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

    @Override
    protected String format(String eventKey, String defaultPattern, Object... args) {
        final String formatted = super.format(eventKey, defaultPattern, args);

        if (codes.containsKey(eventKey)) {
            SGRCode code = codes.get(eventKey);
            return escapeCodeFor(code) + boldifyParams(formatted, code) + escapeCodeFor(RESET);
        }

        return formatted;
    }

    private String boldifyParams(String formatted, SGRCode currentColor) {
        final String valueStart = lookupPattern(PARAMETER_VALUE_START, PARAMETER_VALUE_START);
        final String valueEnd = lookupPattern(PARAMETER_VALUE_END, PARAMETER_VALUE_END);
        return formatted
                .replaceAll(valueStart, escapeCodeFor(BOLD, currentColor))
                .replaceAll(valueEnd, escapeCodeFor(RESET, currentColor));
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

    public void assignCodeToEvent(String eventKey, SGRCode code) {
        codes.put(eventKey, code);
    }
    
    public static enum SGRCode {
        RESET(0),
        BOLD(1),
        DARK(2),
        ITALIC(3),
        UNDERLINE(4),
        BLINK(5),
        RAPID_BLINK(6),
        NEGATIVE(7),
        CONCEALED(8),
        STRIKETHROUGH(9),
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
        ON_WHITE(47);

        private final int code;

        SGRCode(int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return Integer.toString(code);
        }
    }

}
