package org.jbehave.core.reporters;

/**
 * ANSI color codes for colored console output.
 */
public enum ANSIColor {
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

    private final int ansiCode;

    ANSIColor(int ansiCode) {
        this.ansiCode = ansiCode;
    }

    @Override
    public String toString() {
        return Integer.toString(ansiCode);
    }
}
