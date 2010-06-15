package org.jbehave.core.io;

import static org.apache.commons.lang.StringUtils.removeStart;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>
 * Abstraction of a story location, handling cases in which story path is defined
 * as a resource in classpath or as a URL.
 * </p>
 * <p>Given a code location and a story path, it provides the methods:
 * <ul>
 * <li>{@link #getPath()}: the story path</li>
 * <li>{@link #getLocation()}: the story URL, prefixing the code location if story path is not a URL</li>
 * <li>{@link #getName()}: the story path, removing the code location if the story path is a URL</li>
 * </ul>
 * </p>
 */
public class StoryLocation {

	private final URL codeLocation;
	private final String storyPath;
	private final boolean url;

	public StoryLocation(URL codeLocation, String storyPath) {
		this.codeLocation = codeLocation;
		this.storyPath = storyPath;
		this.url = isURL(storyPath);
	}

	public static URL codeLocationFromClass(Class<?> codeLocationClass) {
		return codeLocationClass.getProtectionDomain().getCodeSource().getLocation();
	}

	public static URL codeLocationFromFile(File codeLocationFile) {
		try {
			return codeLocationFile.toURL();
		} catch (MalformedURLException e) {
			throw new InvalidCodeLocation(codeLocationFile);
		}
	}

	public URL getCodeLocation() {
		return codeLocation;
	}

	public String getPath() {
		return storyPath;
	}

	public String getLocation() {
		if (url) {
			return storyPath;
		} else {
			return codeLocation + storyPath;
		}
	}

	public String getName() {
		if (url) {
			return removeStart(storyPath, codeLocation.toString());
		} else {
			return storyPath;
		}
	}

	public boolean isURL() {
		return url;
	}

	private boolean isURL(String storyPath) {
		try {
			new URL(storyPath);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, 
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	@SuppressWarnings("serial")
	public static class InvalidCodeLocation extends RuntimeException {

		public InvalidCodeLocation(File file) {
			super(file.toString());
		}
		
	}
}