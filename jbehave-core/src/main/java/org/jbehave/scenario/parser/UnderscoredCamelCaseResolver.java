package org.jbehave.scenario.parser;

import static java.util.regex.Pattern.compile;

import java.util.regex.Matcher;

import org.jbehave.scenario.RunnableScenario;

/**
 * <p>
 * Resolves scenario names converting the camel-cased Java scenario class to
 * lower-case underscore-separated name eg:
 * "org.jbehave.scenario.ICanLogin.java" -> "org/jbehave/scenario/i_can_login".
 * </p>
 * <p>
 * By default no extension is used, but this can be configured via the
 * constructor so that we can resolve name to eg
 * "org/jbehave/scenario/i_can_login.scenario".
 * </p>
 * <p>
 * The default resolution pattern {@link NUMBERS_AS_LOWER_CASE_LETTERS_PATTERN}
 * treats numbers as lower case letters, eg:
 * "org.jbehave.scenario.ICanLoginTo1Site.java" ->
 * "org/jbehave/scenario/i_can_login_to1_site"
 * </p>
 * <p>
 * Choose {@link NUMBERS_AS_UPPER_CASE_LETTERS_PATTERN} to treat numbers as
 * uper case letters, eg: "org.jbehave.scenario.ICanLoginTo1Site.java" ->
 * "org/jbehave/scenario/i_can_login_to_1_site"
 * </p>
 */
public class UnderscoredCamelCaseResolver extends AbstractScenarioNameResolver {

	public static final String NUMBERS_AS_LOWER_CASE_LETTERS_PATTERN = "([A-Z].*?)([A-Z]|\\z)";
	public static final String NUMBERS_AS_UPPER_CASE_LETTERS_PATTERN = "([A-Z0-9].*?)([A-Z0-9]|\\z)";
	private static final String UNDERSCORE = "_";
	private final String resolutionPattern;

	public UnderscoredCamelCaseResolver() {
		this(DEFAULT_EXTENSION);
	}

	public UnderscoredCamelCaseResolver(String extension) {
		this(extension, NUMBERS_AS_LOWER_CASE_LETTERS_PATTERN);
	}

	public UnderscoredCamelCaseResolver(String extension,
			String resolutionPattern) {
		super(extension);
		this.resolutionPattern = resolutionPattern;
	}

	@Override
	protected String resolveFileName(
			Class<? extends RunnableScenario> scenarioClass) {
		Matcher matcher = compile(resolutionPattern).matcher(
				scenarioClass.getSimpleName());
		int startAt = 0;
		StringBuilder builder = new StringBuilder();
		while (matcher.find(startAt)) {
			builder.append(matcher.group(1).toLowerCase());
			builder.append(UNDERSCORE);
			startAt = matcher.start(2);
		}
		return builder.substring(0, builder.length() - 1);
	}

}
