package org.jbehave.scenario.i18n;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;
import static org.jbehave.scenario.definition.KeyWords.AND;
import static org.jbehave.scenario.definition.KeyWords.AS_A;
import static org.jbehave.scenario.definition.KeyWords.EXAMPLES_TABLE;
import static org.jbehave.scenario.definition.KeyWords.EXAMPLES_TABLE_ROW;
import static org.jbehave.scenario.definition.KeyWords.FAILED;
import static org.jbehave.scenario.definition.KeyWords.GIVEN;
import static org.jbehave.scenario.definition.KeyWords.GIVEN_SCENARIOS;
import static org.jbehave.scenario.definition.KeyWords.IGNORABLE;
import static org.jbehave.scenario.definition.KeyWords.IN_ORDER_TO;
import static org.jbehave.scenario.definition.KeyWords.I_WANT_TO;
import static org.jbehave.scenario.definition.KeyWords.NARRATIVE;
import static org.jbehave.scenario.definition.KeyWords.NOT_PERFORMED;
import static org.jbehave.scenario.definition.KeyWords.PENDING;
import static org.jbehave.scenario.definition.KeyWords.SCENARIO;
import static org.jbehave.scenario.definition.KeyWords.THEN;
import static org.jbehave.scenario.definition.KeyWords.WHEN;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.jbehave.scenario.definition.KeyWords;
import org.jbehave.scenario.i18n.I18nKeyWords.I18nKeywordNotFoundException;
import org.jbehave.scenario.i18n.I18nKeyWords.ResourceBundleNotFoundException;
import org.jbehave.scenario.steps.StepType;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.junit.Test;

public class I18nKeywordsBehaviour {

    private StringEncoder encoder = new StringEncoder("UTF-8", "UTF-8");

    @Test
    public void shouldAllowKeywordsInEnglishAsDefault() throws IOException {
        ensureKeywordsAreLocalisedFor(null, null);
    }

    @Test
    public void shouldAllowKeywordsInADifferentLocale() throws IOException {
        ensureKeywordsAreLocalisedFor(new Locale("it"), null);
    }

    @Test(expected = ResourceBundleNotFoundException.class)
    public void shouldFailIfResourceBundleIsNotFound() throws IOException {
        ensureKeywordsAreLocalisedFor(new Locale("en"), "unknown");
    }

    @Test(expected = I18nKeywordNotFoundException.class)
    public void shouldFailIfKeywordIsNotFound() throws IOException {
        ensureKeywordsAreLocalisedFor(new Locale("es"), null);
    }

    @Test
    public void shouldAllowKeywordsToBeOverriddenInStepsConfiguration() {
        StepsConfiguration configuration = new StepsConfiguration();
        ensureKeywordsAreLocalised(configuration, new Locale("en"));
        configuration.useKeyWords(new I18nKeyWords(new Locale("it")));
        ensureKeywordsAreLocalised(configuration, new Locale("it"));
    }

    private void ensureKeywordsAreLocalised(StepsConfiguration configuration, Locale locale) {
        Map<StepType, String> startingWordsByType = configuration.getStartingWordsByType();
        KeyWords keywords = keyWordsFor(locale, null);
        ensureThat(startingWordsByType.get(StepType.GIVEN), equalTo(keywords.given()));
        ensureThat(startingWordsByType.get(StepType.WHEN), equalTo(keywords.when()));
        ensureThat(startingWordsByType.get(StepType.THEN), equalTo(keywords.then()));
        ensureThat(startingWordsByType.get(StepType.AND), equalTo(keywords.and()));
        ensureThat(startingWordsByType.get(StepType.IGNORABLE), equalTo(keywords.ignorable()));
    }

    private void ensureKeywordsAreLocalisedFor(Locale locale, String bundleName) throws IOException {
        KeyWords keywords = keyWordsFor(locale, bundleName);
        Properties properties = bundleFor(locale);
        ensureKeywordIs(properties, NARRATIVE, keywords.narrative());
        ensureKeywordIs(properties, IN_ORDER_TO, keywords.inOrderTo());
        ensureKeywordIs(properties, AS_A, keywords.asA());
        ensureKeywordIs(properties, I_WANT_TO, keywords.iWantTo());
        ensureKeywordIs(properties, SCENARIO, keywords.scenario());
        ensureKeywordIs(properties, GIVEN_SCENARIOS, keywords.givenScenarios());
        ensureKeywordIs(properties, EXAMPLES_TABLE, keywords.examplesTable());
        ensureKeywordIs(properties, EXAMPLES_TABLE_ROW, keywords.examplesTableRow());
        ensureKeywordIs(properties, GIVEN, keywords.given());
        ensureKeywordIs(properties, WHEN, keywords.when());
        ensureKeywordIs(properties, THEN, keywords.then());
        ensureKeywordIs(properties, AND, keywords.and());
        ensureKeywordIs(properties, IGNORABLE, keywords.ignorable());
        ensureKeywordIs(properties, PENDING, keywords.pending());
        ensureKeywordIs(properties, NOT_PERFORMED, keywords.notPerformed());
        ensureKeywordIs(properties, FAILED, keywords.failed());
    }

    private I18nKeyWords keyWordsFor(Locale locale, String bundleName) {
        if (bundleName == null) {
            return (locale == null ? new I18nKeyWords() : new I18nKeyWords(locale));
        } else {
            return new I18nKeyWords(locale, new StringEncoder(), bundleName, Thread.currentThread()
                    .getContextClassLoader());
        }
    }

    private Properties bundleFor(Locale locale) throws IOException {
        Properties expected = new Properties();
        String bundle = "org/jbehave/scenario/i18n/keywords_" + (locale == null ? "en" : locale.getLanguage())
                + ".properties";
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(bundle);
        if (stream != null) {
            expected.load(stream);
        }
        return expected;
    }

    private void ensureKeywordIs(Properties properties, String key, String value) {
        assertEquals(encoder.encode(properties.getProperty(key, value)), value);
    }

}
