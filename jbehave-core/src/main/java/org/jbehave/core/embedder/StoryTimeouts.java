package org.jbehave.core.embedder;

import java.util.HashMap;
import java.util.Map;
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
		private Pattern validNumber = Pattern.compile("[\\d+]");
		private String pathPattern = "";
		private String timeout = "0";
		private String timeoutAsString;
		private boolean isDefault;

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
			if (validNumber.matcher(timeout).find()) {
				return Long.parseLong(timeout);
			}
			return 0;
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

}
