package org.jbehave.core.expressions;

import java.util.List;

import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.matcher.StringMatcher;
import org.apache.commons.text.matcher.StringMatcherFactory;

class ExpressionArguments {
    private static final char DELIMITER = ',';
    private static final char ESCAPE = '\\';

    private static final StringMatcher QUOTE_MATCHER = StringMatcherFactory.INSTANCE.stringMatcher("\"\"\"");
    private static final StringMatcher TRIMMER_MATCHER = StringMatcherFactory.INSTANCE.trimMatcher();
    private static final IgnoredMatcher IGNORED_MATCHER = new IgnoredMatcher();

    private final List<String> arguments;

    ExpressionArguments(String argumentsAsString) {
        this(argumentsAsString, Integer.MAX_VALUE);
    }

    ExpressionArguments(String argumentsAsString, int argsLimit) {
        StringTokenizer argumentsTokenizer = new StringTokenizer(argumentsAsString)
                .setDelimiterMatcher(new DelimiterMatcher(argsLimit))
                .setQuoteMatcher(QUOTE_MATCHER)
                .setTrimmerMatcher(TRIMMER_MATCHER)
                .setIgnoredMatcher(IGNORED_MATCHER)
                .setIgnoreEmptyTokens(false);
        this.arguments = argumentsTokenizer.getTokenList();
    }

    public List<String> getArguments() {
        return arguments;
    }

    private static final class DelimiterMatcher implements StringMatcher {
        private final int resultsLimit;
        private int totalNumberOfMatches = 1;

        private DelimiterMatcher(int resultsLimit) {
            this.resultsLimit = resultsLimit;
        }

        @Override
        public int isMatch(char[] buffer, int start, int bufferStart, int bufferEnd) {
            if (DELIMITER == buffer[start] && (start == 0 || ESCAPE != buffer[start - 1])) {
                if (resultsLimit > totalNumberOfMatches) {
                    totalNumberOfMatches++;
                    return 1;
                }
            }
            return 0;
        }
    }

    private static final class IgnoredMatcher implements StringMatcher {
        @Override
        public int isMatch(char[] buffer, int start, int bufferStart, int bufferEnd) {
            int next = start + 1;
            return ESCAPE == buffer[start] && next < buffer.length && DELIMITER == buffer[next] ? 1 : 0;
        }
    }
}
