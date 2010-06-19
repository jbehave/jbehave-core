package org.jbehave.core.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A step pattern parser that provides a step matcher which will capture
 * parameters starting with the given prefix in any matching step. Default
 * prefix is $.
 * 
 * @author Elizabeth Keogh
 */
public class RegexPrefixCapturingPatternParser implements StepPatternParser {

	private final String prefix;

	/**
	 * Creates a parser which captures parameters starting with $ in a matching
	 * step.
	 */
	public RegexPrefixCapturingPatternParser() {
		this("$");
	}

	/**
	 * Creates a parser which captures parameters starting with a given prefix
	 * in a matching step.
	 * 
	 * @param prefix the prefix to use in capturing parameters
	 */
	public RegexPrefixCapturingPatternParser(String prefix) {
		this.prefix = prefix;
	}

	public StepMatcher parseStep(String stepPattern) {
		return new RegexStepMatcher(buildPattern(stepPattern),
				extractParameterNames(stepPattern));
	}

	private Pattern buildPattern(String stepPattern) {
		String matchThisButLeaveBrackets = escapeRegexPunctuation(stepPattern);
		String patternToMatchAgainst = replaceParametersWithCapture(
				matchThisButLeaveBrackets, findParametersToReplace(matchThisButLeaveBrackets));
		String matchThisButIgnoreWhitespace = anyWhitespaceWillDo(patternToMatchAgainst);
		return Pattern.compile(matchThisButIgnoreWhitespace, Pattern.DOTALL);
	}

    private String escapeRegexPunctuation(String matchThis) {
        return matchThis.replaceAll("([\\[\\]\\{\\}\\?\\^\\.\\*\\(\\)\\+\\\\])", "\\\\$1");
    }

	private String anyWhitespaceWillDo(String matchThis) {
		return matchThis.replaceAll("\\s+", "\\\\s+");
	}

	private String[] extractParameterNames(String stepPattern) {
		List<String> names = new ArrayList<String>();
		for (Parameter parameter : findParametersToReplace(stepPattern)) {
			names.add(parameter.name);
		}
		return names.toArray(new String[names.size()]);
	}

    private List<Parameter> findParametersToReplace(String matchThisButLeaveBrackets) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        Matcher findingAllPrefixedWords = findAllPrefixedWords().matcher(matchThisButLeaveBrackets);
        while (findingAllPrefixedWords.find()) {
            parameters.add(new Parameter(matchThisButLeaveBrackets, findingAllPrefixedWords.start(),
                    findingAllPrefixedWords.end(), findingAllPrefixedWords.group(2)));
        }
        return parameters;
    }

    private Pattern findAllPrefixedWords() {
        return Pattern.compile("(\\" + prefix + "\\w*)(\\W|\\Z)", Pattern.DOTALL);
    }

	private String replaceParametersWithCapture(String escapedMatch,
			List<Parameter> parameters) {
        String replaced = escapedMatch;
        for (int i = parameters.size(); i > 0; i--) {
            String start = replaced.substring(0, parameters.get(i - 1).start);
            String end = replaced.substring(parameters.get(i - 1).end);
            String whitespaceIfAny = parameters.get(i - 1).whitespaceIfAny;
            replaced = start + "(.*)" + whitespaceIfAny + end;
        }
        return replaced;
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
			this.name = pattern.substring(start + prefix.length(), end).trim();
		}

	}

}
