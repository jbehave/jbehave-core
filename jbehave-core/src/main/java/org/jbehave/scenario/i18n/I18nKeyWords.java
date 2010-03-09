package org.jbehave.scenario.i18n;

import static java.util.ResourceBundle.getBundle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jbehave.scenario.ScenarioClassLoader;
import org.jbehave.scenario.definition.KeyWords;

/**
 * Add i18n support to Keywords, allowing to read the keywords from resource
 * bundles for a given locale.
 */
public class I18nKeyWords extends KeyWords {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final StringEncoder DEFAULT_STRING_ENCODER = new StringEncoder();
    private static final String DEFAULT_BUNDLE_NAME = "org/jbehave/scenario/i18n/keywords";
    private static final ClassLoader DEFAULT_CLASS_LOADER = Thread.currentThread().getContextClassLoader();

    public I18nKeyWords() {
        this(DEFAULT_LOCALE, DEFAULT_STRING_ENCODER, DEFAULT_BUNDLE_NAME, DEFAULT_CLASS_LOADER);
    }

    public I18nKeyWords(Locale locale) {
        this(locale, DEFAULT_STRING_ENCODER, DEFAULT_BUNDLE_NAME, DEFAULT_CLASS_LOADER);
    }

    public I18nKeyWords(Locale locale, StringEncoder encoder) {
        this(locale, encoder, DEFAULT_BUNDLE_NAME, DEFAULT_CLASS_LOADER);
    }

    public I18nKeyWords(Locale locale, StringEncoder encoder, String bundleName) {
        super(keywords(bundleName, locale, encoder, DEFAULT_CLASS_LOADER), encoder);
    }

    public I18nKeyWords(Locale locale, StringEncoder encoder, String bundleName, ClassLoader classLoader) {
        super(keywords(bundleName, locale, encoder, classLoader), encoder);
    }

    private static Map<String, String> keywords(String bundleName, Locale locale, StringEncoder encoder,
            ClassLoader classLoader) {
        ResourceBundle bundle = lookupBunde(bundleName.trim(), locale, classLoader);
        Map<String, String> keywords = new HashMap<String, String>();
        for (String key : KEYWORDS) {
            keywords.put(key, keyword(key, bundle, encoder));
        }
        return keywords;
    }

    private static String keyword(String name, ResourceBundle bundle, StringEncoder encoder) {
        try {
            return encoder.encode(bundle.getString(name));
        } catch (MissingResourceException e) {
            throw new I18nKeywordNotFoundException(name, bundle);
        }
    }

    private static ResourceBundle lookupBunde(String bundleName, Locale locale, ClassLoader classLoader) {
        try {            
            if (classLoader instanceof ScenarioClassLoader) {
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
    public static final class I18nKeywordNotFoundException extends RuntimeException {

        public I18nKeywordNotFoundException(String name, ResourceBundle bundle) {
            super("I18nKeyword " + name + " not found in resource bundle " + bundle);
        }

    }

}
