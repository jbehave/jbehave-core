package org.jbehave.core.steps.needle.configuration;


/**
 * Instantiates new java object by default constructor
 * 
 * @author Jan Galinski, Holisticon AG (jan.galinski@holisticon.de)
 * @author Simon Zambrovski, Holisticon AG (simon.zambrovski@holisticon.de)
 */
public enum CreateInstanceByDefaultConstructor {
	/**
	 * Singleton
	 */
	INSTANCE;

	public final <T> T apply(final Class<T> type) {
		try {
			final T newInstance = type.getConstructor().newInstance();
			return newInstance;
		} catch (final Exception e) {
			throw new IllegalStateException("Can not instantiate Instance by Default Constructor.", e);
		}
	}

}
