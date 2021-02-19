package org.jbehave.core.reporters;

import java.util.HashMap;
import java.util.Map;

import static org.jbehave.core.reporters.SGRCodes.SGRCode.*;

/**
 * <p>
 * Manages {@link SGRCode}s used by {@link ANSIConsoleOutput}
 * </p>
 */
public class SGRCodes {

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

    @SuppressWarnings("serial")
    public static final Map<String, SGRCode> DEFAULT_CODES = new HashMap<String, SGRCode>() {
        {
            put("narrative", BLUE);
            put("beforeScenario", BRIGHT_MAGENTA);
            put("successful", GREEN);
            put("pending", YELLOW);
            put("pendingMethod", YELLOW);
            put("notPerformed", MAGENTA);
            put("comment", BLUE);
            put("ignorable", BLUE);
            put("failed", RED);
            put("cancelled", RED);
            put("restarted", MAGENTA);
            put("highlight", UNDERLINE);
        }
    };

    private final Map<String, SGRCode> codes;

    public SGRCodes() {
        this(DEFAULT_CODES);
    }

    public SGRCodes(Map<String, SGRCode> codes) {
        this.codes = codes;
    }

    public void assignCode(String key, SGRCode code) {
        codes.put(key, code);
    }

    public boolean hasCode(String key) {
        return codes.containsKey(key);
    }

    public SGRCode getCode(String key) {
        if (codes.containsKey(key)) {
            return codes.get(key);
        }

        throw new RuntimeException("No code found for key " + key);
    }

}
