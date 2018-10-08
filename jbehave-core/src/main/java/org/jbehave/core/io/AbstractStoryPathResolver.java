package org.jbehave.core.io;

import org.jbehave.core.Embeddable;

public abstract class AbstractStoryPathResolver implements
        StoryPathResolver {

	static final String DOT_REGEX = "\\.";
	static final String SLASH = "/";
	static final String EMPTY = "";
	static final String DEFAULT_EXTENSION = ".story";

	private final String extension;

	protected AbstractStoryPathResolver(String extension) {
		this.extension = extension;
	}

	@Override
    public String resolve(Class<? extends Embeddable> embeddableClass) {
        return formatPath(resolveDirectory(embeddableClass), resolveName(embeddableClass), extension);
	}

	private String formatPath(String directory, String name,
			String extension) {
		StringBuilder sb = new StringBuilder();
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
			Class<? extends Embeddable> embeddableClass) {
		Package scenarioPackage = embeddableClass.getPackage();
		if (scenarioPackage != null) {
			return scenarioPackage.getName().replaceAll(DOT_REGEX, SLASH);
		}
		return EMPTY;
	}

	protected abstract String resolveName(
			Class<? extends Embeddable> embeddableClass);

}
