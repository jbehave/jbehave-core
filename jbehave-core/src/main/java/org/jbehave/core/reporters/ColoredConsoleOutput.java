package org.jbehave.core.reporters;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.reporters.coloredConsole.ANSIColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.jbehave.core.reporters.coloredConsole.ANSIColor.*;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;

/**
 * <p>
 * Story reporter that outputs as ANSI colored TXT to System.out.
 * </p>
 */
public class ColoredConsoleOutput extends ConsoleOutput {

    private Map<String, ANSIColor> outputTypeColor = new HashMap<String, ANSIColor>() {
        {
            put("successful", GREEN);
            put("pending", YELLOW);
            put("pendingMethod", YELLOW);
            put("notPerformed", YELLOW);
            put("ignorable", CYAN);
            put("failed", RED);
        }
    };

    public ColoredConsoleOutput() {
        super();
    }

    public ColoredConsoleOutput(Keywords keywords) {
        super(keywords);
    }

    public ColoredConsoleOutput(Properties outputPatterns, Keywords keywords, boolean reportFailureTrace) {
        super(outputPatterns, keywords, reportFailureTrace);
    }

    @Override
    protected String format(String outputType, String defaultPattern, Object... args) {
        final String formatted = super.format(outputType, defaultPattern, args);

        if (outputTypeColor.containsKey(outputType)) {
            ANSIColor color = outputTypeColor.get(outputType);
            return escapeCodeFor(color) + boldifyParams(formatted, color) + escapeCodeFor(RESET);
        }

        return formatted;
    }

    private String boldifyParams(String formatted, ANSIColor currentColor) {
        final String valueStart = lookupPattern(PARAMETER_VALUE_START, PARAMETER_VALUE_START);
        final String valueEnd = lookupPattern(PARAMETER_VALUE_END, PARAMETER_VALUE_END);
        return formatted
                .replaceAll(valueStart, escapeCodeFor(BOLD, currentColor))
                .replaceAll(valueEnd, escapeCodeFor(RESET, currentColor));
    }

    private String escapeCodeFor(ANSIColor code) {
        return escape(code + "m");
    }

    private String escapeCodeFor(ANSIColor first, ANSIColor second) {
        return escape(first + ";" + second + "m");
    }

    private String escape(String code) {
        return (char) 27 + "[" + code;
    }

    public void assignColorToOutputType(String outputType, ANSIColor color) {
        outputTypeColor.put(outputType, color);
    }
}
