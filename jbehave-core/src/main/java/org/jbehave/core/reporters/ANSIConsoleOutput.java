package org.jbehave.core.reporters;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;

import static org.jbehave.core.reporters.ANSIConsoleOutput.ANSICode.BLUE;
import static org.jbehave.core.reporters.ANSIConsoleOutput.ANSICode.BOLD;
import static org.jbehave.core.reporters.ANSIConsoleOutput.ANSICode.GREEN;
import static org.jbehave.core.reporters.ANSIConsoleOutput.ANSICode.MAGENTA;
import static org.jbehave.core.reporters.ANSIConsoleOutput.ANSICode.RED;
import static org.jbehave.core.reporters.ANSIConsoleOutput.ANSICode.RESET;
import static org.jbehave.core.reporters.ANSIConsoleOutput.ANSICode.YELLOW;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;

/**
 * <p>
 * Story reporter that outputs as ANSI-coded text to System.out.
 * </p>
 */
public class ANSIConsoleOutput extends ConsoleOutput {

    @SuppressWarnings("serial")
    private Map<String, ANSICode> codes = new HashMap<String, ANSICode>() {
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
            ANSICode code = codes.get(eventKey);
            return escapeCodeFor(code) + boldifyParams(formatted, code) + escapeCodeFor(RESET);
        }

        return formatted;
    }

    private String boldifyParams(String formatted, ANSICode currentColor) {
        final String valueStart = lookupPattern(PARAMETER_VALUE_START, PARAMETER_VALUE_START);
        final String valueEnd = lookupPattern(PARAMETER_VALUE_END, PARAMETER_VALUE_END);
        return formatted
                .replaceAll(valueStart, escapeCodeFor(BOLD, currentColor))
                .replaceAll(valueEnd, escapeCodeFor(RESET, currentColor));
    }

    private String escapeCodeFor(ANSICode code) {
        return escape(code + "m");
    }

    private String escapeCodeFor(ANSICode first, ANSICode second) {
        return escape(first + ";" + second + "m");
    }

    private String escape(String code) {
        return (char) 27 + "[" + code;
    }

    public void assignCodeToEvent(String eventKey, ANSICode code) {
        codes.put(eventKey, code);
    }
    
    public static enum ANSICode {
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

        ANSICode(int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return Integer.toString(code);
        }
    }

}
