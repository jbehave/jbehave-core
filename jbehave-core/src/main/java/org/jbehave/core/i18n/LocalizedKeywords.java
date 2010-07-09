package org.jbehave.core.i18n;

import static java.util.ResourceBundle.getBundle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.EmbedderClassLoader;

/**
 * Add i18n support to Keywords, allowing to read the keywords from resource
 * bundles for a given locale.
 */
public class LocalizedKeywords extends Keywords {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final String DEFAULT_BUNDLE_NAME = "i18n/keywords";
    private static final ClassLoader DEFAULT_CLASS_LOADER = Thread.currentThread().getContextClassLoader();
    private static final Encoding DEFAULT_ENCODING = new Encoding();
    private final Locale locale;

    public LocalizedKeywords() {
        this(DEFAULT_LOCALE);
    }

    public LocalizedKeywords(Locale locale) {
        this(locale, DEFAULT_BUNDLE_NAME, DEFAULT_CLASS_LOADER, DEFAULT_ENCODING);
    }

    public LocalizedKeywords(Locale locale, String bundleName, ClassLoader classLoader, Encoding encoding) {
        super(keywords(bundleName, locale, classLoader), encoding);
        this.locale = locale;
    }

    public Locale getLocale(){
        return locale;
    }
    
    private static Map<String, String> keywords(String bundleName, Locale locale,
            ClassLoader classLoader) {
        ResourceBundle bundle = lookupBunde(bundleName.trim(), locale, classLoader);
        Map<String, String> keywords = new HashMap<String, String>();
        for (String key : KEYWORDS) {
            keywords.put(key, keyword(key, bundle));
        }
        return keywords;
    }

    private static String keyword(String name, ResourceBundle bundle) {
        try {
            return bundle.getString(name);
        } catch (MissingResourceException e) {
            throw new LocalizedKeywordNotFoundException(name, bundle);
        }
    }

    private static ResourceBundle lookupBunde(String bundleName, Locale locale, ClassLoader classLoader) {
        try {            
            if (classLoader instanceof EmbedderClassLoader) {
                return getBundle(bundleName, locale, classLoader);
            }
            return getBundle(bundleName, locale);
        } catch (MissingResourceException e) {
            throw new ResourceBundleNotFoundException(bundleName, locale, classLoader, e);
        }
    }

    @SuppressWarnings("serial")
    public static final class ResourceBundleNotFoundException extends RuntimeException {

        public ResourceBundleNotFoundException(String bundleName, Locale locale, ClassLoader classLoader,
                MissingResourceException cause) {
            super("Resource bundle " + bundleName + " not found for locale " + locale + " in classLoader "
                    + classLoader, cause);
        }

    }

    @SuppressWarnings("serial")
    public static final class LocalizedKeywordNotFoundException extends RuntimeException {

        public LocalizedKeywordNotFoundException(String name, ResourceBundle bundle) {
            super("Keyword" + name + " not found in resource bundle " + bundle);
        }

    }

}
