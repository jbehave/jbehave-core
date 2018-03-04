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

    public LocalizedKeywords(Locale locale, Locale baseLocale) {
        this(locale, baseLocale, DEFAULT_BUNDLE_NAME, DEFAULT_CLASS_LOADER);
    }

    public LocalizedKeywords(Locale locale, String bundleName, ClassLoader classLoader) {
        this(locale, Locale.ENGLISH, bundleName, classLoader);
    }

    public LocalizedKeywords(Locale locale, Locale baseLocale, String bundleName, ClassLoader classLoader) {
        this(locale, baseLocale, bundleName, bundleName, classLoader);
    }

    public LocalizedKeywords(Locale locale, String bundleName, String baseBundleName) {
        this(locale, bundleName, baseBundleName, DEFAULT_CLASS_LOADER);
    }

    public LocalizedKeywords(Locale locale, String bundleName, String baseBundleName, ClassLoader classLoader) {
        this(locale, locale, bundleName, baseBundleName, classLoader);
    }

    public LocalizedKeywords(Locale locale, Locale baseLocale, String bundleName, String baseBundleName, ClassLoader classLoader) {
        super(keywords(locale, bundleName, baseLocale, baseBundleName, classLoader));
        this.locale = locale;
    }

    private static Map<String, String> keywords(Locale locale, String bundleName, Locale baseLocale,
                                                String baseBundleName, ClassLoader classLoader) {
        ResourceBundle bundle = findBunde(bundleName, locale, classLoader);
        ResourceBundle baseBundle = findBunde(baseBundleName, baseLocale, classLoader);
        Map<String, String> keywords = new HashMap<String, String>();
        for (String key : KEYWORDS) {
            try {
                keywords.put(key, bundle.getString(key));
            } catch (MissingResourceException e) {
                if (locale == baseLocale && bundleName.equals(baseBundleName) ) {
                    throw new LocalizedKeywordNotFound(key, bundleName, locale);
                } else {
                    keywords.put(key, baseBundle.getString(key));
                }
            }
        }
        return keywords;
    }

    private static ResourceBundle findBunde(String bundleName, Locale locale, ClassLoader classLoader) {
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

    public Locale getLocale() {
        return locale;
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
            super("Keyword " + key + " not found for locale " + locale + " in bundle " + bundleName);
        }

    }

}
