package org.jbehave.core.i18n;

import org.jbehave.core.StoryClassLoader;
import org.jbehave.core.model.Keywords;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Add i18n support to Keywords, allowing to read the keywords from resource
 * bundles for a given locale.
 */
public class LocalizedKeywords extends Keywords {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final StringCoder DEFAULT_STRING_ENCODER = new StringCoder();
    private static final String DEFAULT_BUNDLE_NAME = "org/jbehave/core/i18n/keywords";
    private static final ClassLoader DEFAULT_CLASS_LOADER = Thread.currentThread().getContextClassLoader();

    public LocalizedKeywords() {
        this(DEFAULT_LOCALE, DEFAULT_STRING_ENCODER, DEFAULT_BUNDLE_NAME, DEFAULT_CLASS_LOADER);
    }

    public LocalizedKeywords(Locale locale) {
        this(locale, DEFAULT_STRING_ENCODER, DEFAULT_BUNDLE_NAME, DEFAULT_CLASS_LOADER);
    }

    public LocalizedKeywords(Locale locale, StringCoder encoder) {
        this(locale, encoder, DEFAULT_BUNDLE_NAME, DEFAULT_CLASS_LOADER);
    }

    public LocalizedKeywords(Locale locale, StringCoder encoder, String bundleName) {
        super(keywords(bundleName, locale, encoder, DEFAULT_CLASS_LOADER), encoder);
    }

    public LocalizedKeywords(Locale locale, StringCoder encoder, String bundleName, ClassLoader classLoader) {
        super(keywords(bundleName, locale, encoder, classLoader), encoder);
    }

    private static Map<String, String> keywords(String bundleName, Locale locale, StringCoder encoder,
            ClassLoader classLoader) {
        ResourceBundle bundle = lookupBunde(bundleName.trim(), locale, classLoader);
        Map<String, String> keywords = new HashMap<String, String>();
        for (String key : KEYWORDS) {
            keywords.put(key, keyword(key, bundle, encoder));
        }
        return keywords;
    }

    private static String keyword(String name, ResourceBundle bundle, StringCoder encoder) {
        try {
            return encoder.canonicalize(bundle.getString(name));
        } catch (MissingResourceException e) {
            throw new LocalizedKeywordNotFoundException(name, bundle);
        }
    }

    private static ResourceBundle lookupBunde(String bundleName, Locale locale, ClassLoader classLoader) {
        try {            
            if (classLoader instanceof StoryClassLoader) {
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
            super("I18nKeyword " + name + " not found in resource bundle " + bundle);
        }

    }

}
