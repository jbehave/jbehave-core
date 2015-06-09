package org.jbehave.core.i18n;

import static java.util.ResourceBundle.getBundle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jbehave.core.configuration.Keywords;

/**
 * Adds i18n support to Keywords, allowing to read the keywords from resource
 * bundles for a given locale.
 */
public class LocalizedKeywords extends Keywords {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final String DEFAULT_BUNDLE_NAME = "i18n/keywords";
    private static final ClassLoader DEFAULT_CLASS_LOADER = LocalizedKeywords.class.getClassLoader();
    private final Locale locale;

    public LocalizedKeywords() {
        this(DEFAULT_LOCALE);
    }

    public LocalizedKeywords(Locale locale) {
        this(locale, DEFAULT_BUNDLE_NAME, DEFAULT_CLASS_LOADER);
    }

    public LocalizedKeywords(Locale locale, String bundleName, ClassLoader classLoader) {
        super(keywords(bundleName, classLoader, locale));
        this.locale = locale;
    }

    public Locale getLocale(){
        return locale;
    }
    
	private static Map<String, String> keywords(String bundleName,
			ClassLoader classLoader, Locale locale) {
		ResourceBundle bundle = findBunde(bundleName, classLoader, locale);
		ResourceBundle defaultBundle = findBunde(bundleName, classLoader,
				Locale.ENGLISH);
		Map<String, String> keywords = new HashMap<String, String>();
		for (String key : KEYWORDS) {
			try {
				keywords.put(key, bundle.getString(key));
			} catch (MissingResourceException e) {
				if (locale == Locale.ENGLISH) {
					throw new LocalizedKeywordNotFound(key, bundleName, locale);
				} else {
					keywords.put(key, defaultBundle.getString(key));
				}
			}
		}
		return keywords;
	}

    private static ResourceBundle findBunde(String bundleName, ClassLoader classLoader, Locale locale) {
    	String name = bundleName.trim();
        try {            
            if (classLoader != null) {
                return getBundle(name, locale, classLoader);
            }
            return getBundle(name, locale);
        } catch (MissingResourceException e) {
            throw new ResourceBundleNotFound(name, locale, classLoader, e);
        }
    }

    @SuppressWarnings("serial")
    public static class ResourceBundleNotFound extends RuntimeException {

        public ResourceBundleNotFound(String bundleName, Locale locale, ClassLoader classLoader,
                MissingResourceException cause) {
            super("Resource bundle " + bundleName + " not found for locale " + locale + " in classLoader "
                    + classLoader, cause);
        }

    }

    @SuppressWarnings("serial")
    public static class LocalizedKeywordNotFound extends RuntimeException {

        public LocalizedKeywordNotFound(String key, String bundleName, Locale locale) {
            super("Keyword " + key + " not found for locale " + locale + " in bundle "+bundleName);
        }

    }

}
