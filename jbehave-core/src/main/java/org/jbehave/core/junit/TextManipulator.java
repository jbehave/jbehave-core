package org.jbehave.core.junit;

import java.util.HashSet;
import java.util.Set;

public class TextManipulator {
    private static final char ONE_DOT_LEADER = '\u2024';

    private final Set<String> uniqueSet = new HashSet<>();

    public String uniquify(String value) {
        String unique = escape(value);
        while (uniqueSet.contains(unique)) {
            unique += '\u200B'; // zero-width-space
        }
        uniqueSet.add(unique);
        return unique;
    }

    public static String escape(String value) {
        return value.replace('.', ONE_DOT_LEADER)
                .replaceAll("[\r\n]+", ", ")
                .replaceAll("[()]", "|");
    }

}
