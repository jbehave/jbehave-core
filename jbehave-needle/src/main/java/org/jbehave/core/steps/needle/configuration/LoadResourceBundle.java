package org.jbehave.core.steps.needle.configuration;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Null safe Resource Loader. If ResourceBundle does not exist, an empty Bundle is returned.
 * 
 * @author Jan Galinski, Holisticon AG (jan.galinski@holisticon.de)
 * @author Simon Zambrovski, Holisticon AG (simon.zambrovski@holisticon.de)
 */
public enum LoadResourceBundle {
	INSTANCE;

	public static final ResourceBundle EMPTY_RESOURCE_BUNDLE = new ResourceBundle() {

		@Override
		public Enumeration<String> getKeys() {
			return new Enumeration<String>() {

				@Override
                public boolean hasMoreElements() {
					return false;
				}

				@Override
                public String nextElement() {
					return null;
				}
			};
		}

		@Override
		protected Object handleGetObject(final String key) {
			return "";
		}
	};

	public final ResourceBundle apply(final String resourceName) {
		if (resourceName == null || "".equals(resourceName.trim())) {
			throw new IllegalArgumentException("resourceName must not be null or empty!");
		}

		try {
			return ResourceBundle.getBundle(resourceName);
		} catch (final MissingResourceException e) {
			return EMPTY_RESOURCE_BUNDLE;
		}
	}

}
