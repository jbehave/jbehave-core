package org.jbehave.core.parser;

import org.jbehave.core.RunnableStory;

public abstract class AbstractStoryPathResolver implements
        StoryPathResolver {

	static final String DOT_REGEX = "\\.";
	static final String SLASH = "/";
	static final String EMPTY = "";
	static final String DEFAULT_EXTENSION = "";

	private final String extension;

	protected AbstractStoryPathResolver() {
		this(DEFAULT_EXTENSION);
	}

	protected AbstractStoryPathResolver(String extension) {
		this.extension = extension;
	}

	public String resolve(Class<? extends RunnableStory> storyClass) {
        return formatPath(resolveDirectory(storyClass), resolveName(storyClass), extension);
	}

	private String formatPath(String directory, String name,
			String extension) {
		StringBuffer sb = new StringBuffer();
		if (directory.length() > 0) {
			sb.append(directory).append(SLASH);
		}
		sb.append(name);
		if (extension.length() > 0) {
			sb.append(extension);
		}
		return sb.toString();
	}

	protected String resolveDirectory(
			Class<? extends RunnableStory> scenarioClass) {
		Package scenarioPackage = scenarioClass.getPackage();
		if (scenarioPackage != null) {
			return scenarioPackage.getName().replaceAll(DOT_REGEX, SLASH);
		}
		return EMPTY;
	}

	protected abstract String resolveName(
			Class<? extends RunnableStory> storyClass);

}