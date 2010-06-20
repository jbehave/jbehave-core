package org.jbehave.core.configuration;


public class AnnotatedConfigurationFactory implements ConfigurationFactory {

	private Configuration configuration;

	public AnnotatedConfigurationFactory(
			Object annotatedRunner) {
		
		configuration = AnnotatedConfigurationBuilder.buildConfiguration(annotatedRunner);
	}

	public Configuration createConfiguration() {
		
		return configuration;
	}

}
