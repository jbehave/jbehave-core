package org.jbehave.core.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.steps.StepType;

/**
 * <p>A step pattern parser that provides a step matcher which will capture
 * parameters starting with the given prefix in any matching step. Default
 * prefix is $.</p>
 * 
 * <p>The parameter names are by default assumed to be any unicode-supported
 * alphanumeric sequence, not limited to ASCII (see
 * http://www.regular-expressions.info/unicode.html), i.e. corresponding to
 * character class [\p{L}\p{N}\p{Pc}]. A different character class can optionally be
 * provided.</p>
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public class RegexPrefixCapturingPatternParser implements StepPatternParser {

    /**
     * The default prefix to identify parameter names
     */
    private static final String DEFAULT_PREFIX = "$";
    /**
     * The default character class to match the parameter names.
     */
    private static final String DEFAULT_CHARACTER_CLASS = "[\\p{L}\\p{N}\\p{Pc}]";

    private final String prefix;
    private final String characterClass;

    /**
     * Creates a parser which captures parameters starting with $ in a matching
     * step and whose names are alphanumeric sequences.
     */
    public RegexPrefixCapturingPatternParser() {
        this(DEFAULT_PREFIX);
    }

    /**
     * Creates a parser which captures parameters starting with a given prefix
     * in a matching step and whose names are alphanumeric sequences
     *
     * @param prefix
     *            the prefix to use in capturing parameters
     */
    public RegexPrefixCapturingPatternParser(String prefix) {
        this(prefix, DEFAULT_CHARACTER_CLASS);
    }

    /**
     * Creates a parser which captures parameters starting with a given prefix
     * in a matching step and a given character class.
     *
     * @param prefix
     *            the prefix to use in capturing parameters
     * @param characterClass
     *            the regex character class to find parameter names
     */
    public RegexPrefixCapturingPatternParser(String prefix,
            String characterClass) {
        this.prefix = prefix;
        this.characterClass = characterClass;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public StepMatcher parseStep(StepType stepType, String stepPattern) {
        String escapingPunctuation = escapingPunctuation(stepPattern);
        List<Parameter> parameters = findParameters(escapingPunctuation);
        Pattern regexPattern = buildPattern(escapingPunctuation, parameters);
        return new RegexStepMatcher(stepType, escapingPunctuation, regexPattern,
                parameterNames(parameters));
    }

    private Pattern buildPattern(String stepPattern, List<Parameter> parameters) {
        return Pattern.compile(
                parameterCapturingRegex(stepPattern, parameters),
                Pattern.DOTALL);
    }

    private String[] parameterNames(List<Parameter> parameters) {
        List<String> names = new ArrayList<>();
        for (Parameter parameter : parameters) {
            names.add(parameter.name);
        }
        return names.toArray(new String[names.size()]);
    }

    private List<Parameter> findParameters(String pattern) {
        List<Parameter> parameters = new ArrayList<>();
        Matcher findingAllParameterNames = findingAllParameterNames().matcher(
                pattern);
        while (findingAllParameterNames.find()) {
            parameters.add(new Parameter(pattern, findingAllParameterNames
                    .start(), findingAllParameterNames.end(),
                    findingAllParameterNames.group(2)));
        }
        return parameters;
    }

    private Pattern findingAllParameterNames() {
        return Pattern.compile("(\\" + prefix + characterClass + "*)(\\W|\\Z)",
                Pattern.DOTALL);
    }

    private String escapingPunctuation(String pattern) {
        return pattern.replaceAll("([\\[\\]\\{\\}\\?\\^\\.\\*\\(\\)\\+\\\\])",
                "\\\\$1");
    }

    private String ignoringWhitespace(String pattern) {
        return pattern.replaceAll("\\s+", "\\\\s+");
    }

    private String parameterCapturingRegex(String stepPattern,
            List<Parameter> parameters) {
        String regex = stepPattern;
        String capture = "(.*)";
        for (int i = parameters.size(); i > 0; i--) {
            Parameter parameter = parameters.get(i - 1);
            String start = regex.substring(0, parameter.start);
            String end = regex.substring(parameter.end);
            String whitespaceIfAny = parameter.whitespaceIfAny;
            regex = start + capture + whitespaceIfAny + end;
        }
        return ignoringWhitespace(regex);
    }

    private class Parameter {
        private final int start;
        private final int end;
        private final String whitespaceIfAny;
        private final String name;

        public Parameter(String pattern, int start, int end,
                String whitespaceIfAny) {
            this.start = start;
            this.end = end;
            this.whitespaceIfAny = whitespaceIfAny;
            this.name = pattern.substring(start + prefix.length(),
                    end - whitespaceIfAny.length()).trim();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
