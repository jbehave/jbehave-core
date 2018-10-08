package org.jbehave.core.i18n;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jbehave.core.configuration.Keywords.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords.ResourceBundleNotFound;
import org.jbehave.core.steps.StepType;
import org.junit.Test;

public class LocalizedKeywordsBehaviour {

    @Test
    public void shouldAllowKeywordsInEnglishAsDefault() throws IOException {
        ensureKeywordsAreLocalisedFor(null);
    }

    @Test
    public void shouldUseEnglishAsBaseLocaleIfKeywordIsNotFound() throws IOException {
        ensureKeywordsAreLocalisedFor(new Locale("mk"));
    }

    @Test
    public void shouldUseConfiguredBaseLocaleIfKeywordIsNotFound() throws IOException {
        ensureKeywordsAreLocalisedFor(new Locale("mk"),  Locale.ENGLISH);
    }

    @Test
    public void shouldAllowSynonymsToOverrideABaseLocale() {
        Keywords keywords = new LocalizedKeywords(new Locale("sy"), Locale.ENGLISH);
        assertThat(keywords.given(), equalTo("Given|Giveth"));
        assertThat(keywords.and(), equalTo("And|With"));
    }

    @Test
    public void shouldAllowSynonymsToOverrideABaseBundleForSameLocale() {
        Keywords keywords = new LocalizedKeywords(new Locale("en"), "i18n/synonyms", "i18n/keywords" );
        assertThat(keywords.given(), equalTo("Given|Giveth"));
        assertThat(keywords.and(), equalTo("And|With"));
    }

    @Test
    public void shouldAllowKeywordsInDifferentLocales() throws IOException {
        ensureKeywordsAreLocalisedFor(new Locale("de"));
        ensureKeywordsAreLocalisedFor(new Locale("en"));
        ensureKeywordsAreLocalisedFor(new Locale("es"));
        ensureKeywordsAreLocalisedFor(new Locale("fr"));
        ensureKeywordsAreLocalisedFor(new Locale("fi"));
        ensureKeywordsAreLocalisedFor(new Locale("hu"));
        ensureKeywordsAreLocalisedFor(new Locale("it"));
        ensureKeywordsAreLocalisedFor(new Locale("no"));
        ensureKeywordsAreLocalisedFor(new Locale("pl"));
        ensureKeywordsAreLocalisedFor(new Locale("pt"));
        ensureKeywordsAreLocalisedFor(new Locale("ru"));
        ensureKeywordsAreLocalisedFor(new Locale("ru_sbt"));
        ensureKeywordsAreLocalisedFor(new Locale("sv"));
        ensureKeywordsAreLocalisedFor(new Locale("tr"));
        ensureKeywordsAreLocalisedFor(new Locale("zh_CN"));
        ensureKeywordsAreLocalisedFor(new Locale("zh_TW"));
    }

    @Test
    public void shouldShowKeywordsInToStringRepresentations() {
        LocalizedKeywords it = keywordsFor(new Locale("it"));
        LocalizedKeywords pt = keywordsFor(new Locale("pt"));
        assertThat(it.toString(), not(equalTo(pt.toString())));
    }

    
    @Test(expected = ResourceBundleNotFound.class)
    public void shouldFailIfResourceBundleIsNotFound() throws IOException {
        ensureKeywordsAreLocalisedFor(new Locale("en"), "unknown");
    }

    @Test
    public void shouldProvideClassLoaderPathInMessageIfResourceBundleIsNotFound() throws IOException {
        try {
            ensureKeywordsAreLocalisedFor(new Locale("en"), "unknown");
        } catch ( ResourceBundleNotFound e ){
            String path = StringUtils.substringAfter(e.getMessage(),"Resource bundle unknown not found for locale en in classLoader");
            assertThat(path.split(ResourceBundleNotFound.PATH_SEPARATOR).length, greaterThan(0));
        }
    }

    @Test
    public void shouldAllowKeywordsToBeConfigured() {
        Configuration configuration = new MostUsefulConfiguration();
        ensureKeywordsAreLocalised(configuration, new Locale("en"));
        configuration.useKeywords(new LocalizedKeywords(new Locale("it")));
        ensureKeywordsAreLocalised(configuration, new Locale("it"));
    }

    private void ensureKeywordsAreLocalised(Configuration configuration, Locale locale) {
        Keywords keywords = keywordsFor(locale, null, null);
        Map<StepType, String> startingWordsByType = keywords.startingWordsByType();
        assertThat(startingWordsByType.get(StepType.GIVEN), equalTo(keywords.given()));
        assertThat(startingWordsByType.get(StepType.WHEN), equalTo(keywords.when()));
        assertThat(startingWordsByType.get(StepType.THEN), equalTo(keywords.then()));
        assertThat(startingWordsByType.get(StepType.AND), equalTo(keywords.and()));
        assertThat(startingWordsByType.get(StepType.IGNORABLE), equalTo(keywords.ignorable()));
    }

    private void ensureKeywordsAreLocalisedFor(Locale locale) throws IOException {
        ensureKeywordsAreLocalisedFor(locale, (String)null);
    }

    private void ensureKeywordsAreLocalisedFor(Locale locale, Locale baseLocale) throws IOException {
        ensureKeywordsAreLocalisedFor(locale, baseLocale, null);
    }

    private void ensureKeywordsAreLocalisedFor(Locale locale, String bundleName) throws IOException {
        ensureKeywordsAreLocalisedFor(locale, null, bundleName);
    }

    private void ensureKeywordsAreLocalisedFor(Locale locale, Locale baseLocale, String bundleName) throws IOException {
        Keywords keywords = keywordsFor(locale, baseLocale, bundleName, null);
        Properties properties = bundleFor(locale);
        ensureKeywordIs(properties, META, keywords.meta());
        ensureKeywordIs(properties, META_PROPERTY, keywords.metaProperty());
        ensureKeywordIs(properties, NARRATIVE, keywords.narrative());
        ensureKeywordIs(properties, IN_ORDER_TO, keywords.inOrderTo());
        ensureKeywordIs(properties, AS_A, keywords.asA());
        ensureKeywordIs(properties, I_WANT_TO, keywords.iWantTo());
        ensureKeywordIs(properties, SO_THAT, keywords.soThat());
        ensureKeywordIs(properties, SCENARIO, keywords.scenario());
        ensureKeywordIs(properties, GIVEN_STORIES, keywords.givenStories());
        ensureKeywordIs(properties, LIFECYCLE, keywords.lifecycle());
        ensureKeywordIs(properties, BEFORE, keywords.before());
        ensureKeywordIs(properties, AFTER, keywords.after());
        ensureKeywordIs(properties, EXAMPLES_TABLE, keywords.examplesTable());
        ensureKeywordIs(properties, EXAMPLES_TABLE_ROW, keywords.examplesTableRow());
        ensureKeywordIs(properties, EXAMPLES_TABLE_HEADER_SEPARATOR, keywords.examplesTableHeaderSeparator());
        ensureKeywordIs(properties, EXAMPLES_TABLE_VALUE_SEPARATOR, keywords.examplesTableValueSeparator());
        ensureKeywordIs(properties, EXAMPLES_TABLE_IGNORABLE_SEPARATOR, keywords.examplesTableIgnorableSeparator());
        ensureKeywordIs(properties, GIVEN, keywords.given());
        ensureKeywordIs(properties, WHEN, keywords.when());
        ensureKeywordIs(properties, THEN, keywords.then());
        ensureKeywordIs(properties, AND, keywords.and());
        ensureKeywordIs(properties, IGNORABLE, keywords.ignorable());
        ensureKeywordIs(properties, PENDING, keywords.pending());
        ensureKeywordIs(properties, NOT_PERFORMED, keywords.notPerformed());
        ensureKeywordIs(properties, FAILED, keywords.failed());
        ensureKeywordIs(properties, DRY_RUN, keywords.dryRun());
        ensureKeywordIs(properties, STORY_CANCELLED, keywords.storyCancelled());
        ensureKeywordIs(properties, SCOPE, keywords.scope());
        ensureKeywordIs(properties, SCOPE_SCENARIO, keywords.scopeScenario());
        ensureKeywordIs(properties, SCOPE_STORY, keywords.scopeStory());
        ensureKeywordIs(properties, META_FILTER, keywords.metaFilter());
        ensureKeywordIs(properties, OUTCOME, keywords.outcome());
        ensureKeywordIs(properties, OUTCOME_ANY, keywords.outcomeAny());
        ensureKeywordIs(properties, OUTCOME_SUCCESS, keywords.outcomeSuccess());
        ensureKeywordIs(properties, OUTCOME_FAILURE, keywords.outcomeFailure());
        ensureKeywordIs(properties, OUTCOME_DESCRIPTION, keywords.outcomeDescription());
        ensureKeywordIs(properties, OUTCOME_VALUE, keywords.outcomeValue());
        ensureKeywordIs(properties, OUTCOME_MATCHER, keywords.outcomeMatcher());
        ensureKeywordIs(properties, OUTCOME_VERIFIED, keywords.outcomeVerified());
    }
    
    private LocalizedKeywords keywordsFor(Locale locale) {
        return keywordsFor(locale, null, null);
    }
        
    private LocalizedKeywords keywordsFor(Locale locale, String bundleName, ClassLoader classLoader) {
        return keywordsFor(locale, null, bundleName, classLoader);
    }

    private LocalizedKeywords keywordsFor(Locale locale, Locale baseLocale, String bundleName, ClassLoader classLoader) {
        ClassLoader cl = classLoader != null ? classLoader : this.getClass().getClassLoader();
        LocalizedKeywords keywords;
        if (bundleName == null) {
            keywords = (locale == null ? new LocalizedKeywords() : new LocalizedKeywords(locale));
        } else {
            keywords = (baseLocale == null ? new LocalizedKeywords(locale, bundleName, cl) : new LocalizedKeywords(locale, baseLocale, bundleName, cl));
        }
        if ( locale != null ){
            assertThat(keywords.getLocale(), equalTo(locale));
        }
        return keywords;
    }

    private Properties bundleFor(Locale locale) throws IOException {
        Properties expected = new Properties();
        String bundle = "i18n/keywords_" + (locale == null ? "en" : locale.getLanguage())
                + ".properties";
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(bundle);
        if (stream != null) {
            expected.load(stream);
        }
        return expected;
    }

    private void ensureKeywordIs(Properties properties, String key, String value) {
        assertThat(properties.getProperty(key, value), equalTo(value));
    }

}
