package org.jbehave.core.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author Valery_Yatsynovich
 */
class JUnitStringDecoratorBehaviour
{
    @Test
    void shouldReplaceParenthesesWithVerticalBars() {
        String actual = JUnitStringDecorator.getJunitSafeString("some string with (parentheses)");
        assertEquals("some string with |parentheses|", actual);
    }

    @Test
    void shouldReplaceCrLfWithCommas() {
        String actual = JUnitStringDecorator.getJunitSafeString("some\n\r string with \r\n\ncrlf\n\n");
        assertEquals("some,  string with , crlf, ", actual);
    }

    @Test
    void shouldReplaceDotWithOneDotLeader() {
        String actual = JUnitStringDecorator.getJunitSafeString("some string. with dots.");
        assertEquals("some string\u2024 with dots\u2024", actual);
    }
}
