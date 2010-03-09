package org.jbehave.scenario.parser;

import org.jbehave.scenario.RunnableScenario;

public abstract class AbstractScenarioNameResolver implements
		ScenarioNameResolver {

	static final String DOT_REGEX = "\\.";
	static final String SLASH = "/";
	static final String EMPTY = "";
	static final String DEFAULT_EXTENSION = "";
	static final String PATH_PATTERN = "{0}/{1}{2}";

	private final String extension;

	protected AbstractScenarioNameResolver() {
		this(DEFAULT_EXTENSION);
	}

	protected AbstractScenarioNameResolver(String extension) {
		this.extension = extension;
	}

	public String resolve(Class<? extends RunnableScenario> scenarioClass) {
		String directoryName = resolveDirectoryName(scenarioClass);
		String fileName = resolveFileName(scenarioClass);
		return formatName(directoryName, fileName, extension);
	}

	private String formatName(String directoryName, String fileName,
			String extension) {
		StringBuffer sb = new StringBuffer();
		if (directoryName.length() > 0) {
			sb.append(directoryName).append(SLASH);
		}
		sb.append(fileName);
		if (extension.length() > 0) {
			sb.append(extension);
		}
		return sb.toString();
	}

	protected String resolveDirectoryName(
			Class<? extends RunnableScenario> scenarioClass) {
		Package scenarioPackage = scenarioClass.getPackage();
		if (scenarioPackage != null) {
			return scenarioPackage.getName().replaceAll(DOT_REGEX, SLASH);
		}
		return EMPTY;
	}

	protected abstract String resolveFileName(
			Class<? extends RunnableScenario> scenarioClass);

}