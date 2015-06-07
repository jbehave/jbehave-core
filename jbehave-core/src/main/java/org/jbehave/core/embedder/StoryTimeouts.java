package org.jbehave.core.embedder;

import static java.util.regex.Pattern.compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.plexus.util.StringUtils;
import org.jbehave.core.model.Story;

public class StoryTimeouts {

	private EmbedderControls embedderControls;
	private EmbedderMonitor embedderMonitor;

	public StoryTimeouts(EmbedderControls embedderControls,
			EmbedderMonitor embedderMonitor) {
		this.embedderControls = embedderControls;
		this.embedderMonitor = embedderMonitor;
	}

	public long getTimeoutInSecs(Story story) {
		// timeout by path
		Map<String, StoryTimeout> timeouts = asMap(embedderControls
				.storyTimeouts());
		for (StoryTimeout timeout : timeouts.values()) {
			if (timeout.allowedByPath(story.getPath())) {
				long timeoutInSecs = timeout.getTimeoutInSecs();
				embedderMonitor.usingTimeout(story.getName(), timeoutInSecs);
				return timeoutInSecs;
			}
		}

		// look for default timeout
		for (StoryTimeout timeout : timeouts.values()) {
			if (timeout.isDefault()) {
				long timeoutInSecs = timeout.getTimeoutInSecs();
				embedderMonitor.usingTimeout(story.getName(), timeoutInSecs);
				return timeoutInSecs;
			}
		}

		// default to 300
		long timeoutInSecs = 300;
		embedderMonitor.usingTimeout(story.getName(), timeoutInSecs);
		return timeoutInSecs;
	}

	private Map<String, StoryTimeout> asMap(String timeoutsAsString) {
		Map<String, StoryTimeout> timeouts = new HashMap<String, StoryTimeout>();
		if (StringUtils.isBlank(timeoutsAsString)) {
			return timeouts;
		}

		for (String timeoutAsString : timeoutsAsString.split(",")) {
			StoryTimeout timeout = new StoryTimeout(timeoutAsString);
			timeouts.put(timeout.getPathPattern(), timeout);
		}
		return timeouts;
	}

	public static class StoryTimeout {
		private String pathPattern = "";
		private String timeout = "0";
		private String timeoutAsString;
		private boolean isDefault;
		private List<TimeoutParser> parsers = new ArrayList<TimeoutParser>();

		public StoryTimeout(String timeoutAsString) {
			this.timeoutAsString = timeoutAsString;
			if (timeoutAsString.contains(":")) {
				String[] timeoutByPath = timeoutAsString.split(":");
				pathPattern = timeoutByPath[0];
				timeout = timeoutByPath[1];
			} else {
				isDefault = true;
				timeout = timeoutAsString;
			}
			parsers.add(new SimpleTimeoutParser());
			parsers.add(new DigitTimeoutParser());
		}

		public boolean allowedByPath(String path) {
			if (path != null) {
				return path.matches(regexOf(pathPattern));
			}
			return false;
		}

		public boolean isDefault() {
			return isDefault;
		}

		public String getPathPattern() {
			return pathPattern;
		}

		public long getTimeoutInSecs() {
			for (TimeoutParser parser : parsers) {
				if (parser.isValid(timeout)) {
					return parser.asSeconds(timeout);
				}
			}
			throw new TimeoutFormatException("No format found for timeout: "+timeout);
		}

		public String getTimeoutAsString() {
			return timeoutAsString;
		}

		private String regexOf(String pattern) {
			try {
				// check if pattern is already a valid regex
				Pattern.compile(pattern);
				return pattern;
			} catch (PatternSyntaxException e) {
				// assume Ant-style pattern: **/path/*.story
				return pattern.replace("*", ".*");
			}
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this,
					ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}

	public static interface TimeoutParser {

		boolean isValid(String timeout);

		long asSeconds(String timeout);

	}

	/**
	 * A simple parser for timeouts of format: 1d 2h 30m 15s.
	 */
	public static class SimpleTimeoutParser implements TimeoutParser {

		private static final String UNIT_PATTERN = "[a-zA-Z]+";
		private static final Pattern TIMEOUT_PATTERN = compile("(\\d+)\\s*("
				+ UNIT_PATTERN + ")");
		private Map<String, Long> units = new HashMap<String, Long>();

		public SimpleTimeoutParser() {
			addUnit("d", 24 * 3600).addUnit("h", 3600).addUnit("m", 60)
					.addUnit("s", 1);
		}

		private SimpleTimeoutParser addUnit(String unit, long value) {
			if (!unit.matches(UNIT_PATTERN)) {
				throw new TimeoutFormatException("Unit '" + unit
						+ "' must be a non-numeric word");
			}
			if (value < 0) {
				throw new TimeoutFormatException("Unit value '" + value
						+ "' cannot be negative");
			}
			units.put(unit, Long.valueOf(value));
			return this;
		}

		public boolean isValid(String timeout) {
			return TIMEOUT_PATTERN.matcher(timeout).find();
		}

		public long asSeconds(String timeout) {
			long total = 0;
			Matcher matcher = TIMEOUT_PATTERN.matcher(timeout);
			while (matcher.find()) {
				long value = Long.parseLong(matcher.group(1));
				String unit = matcher.group(2);
				if (!units.containsKey(unit)) {
					throw new TimeoutFormatException("Unrecognized unit: "
							+ unit);
				}
				total += units.get(unit).longValue() * value;
			}
			return total;
		}

	}

	/**
	 * A digit parser for timeouts
	 */
	public static class DigitTimeoutParser implements TimeoutParser {

		private static final Pattern TIMEOUT_PATTERN = compile("(\\d+)");

		public boolean isValid(String timeout) {
			return TIMEOUT_PATTERN.matcher(timeout).find();
		}

		public long asSeconds(String timeout) {
			return Long.parseLong(timeout);
		}

	}

	@SuppressWarnings("serial")
	public static class TimeoutFormatException extends IllegalArgumentException {

		public TimeoutFormatException(String message) {
			super(message);
		}

	}
}
