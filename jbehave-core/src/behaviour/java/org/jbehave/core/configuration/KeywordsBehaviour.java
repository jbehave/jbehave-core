package org.jbehave.core.configuration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.jbehave.core.configuration.Keywords.KeywordNotFoundException;
import org.jbehave.core.i18n.StringCoder;
import org.junit.Test;

public class KeywordsBehaviour {

    @Test
    public void shouldHaveAllKeywordsSetByDefault() throws IOException {
        Keywords keywords = new Keywords();
        assertEquals("Narrative:", keywords.narrative());
        assertEquals("Scenario:", keywords.scenario());
        assertEquals("GivenStories:", keywords.givenStories());
        assertEquals("Examples:", keywords.examplesTable());
        assertEquals("Example:", keywords.examplesTableRow());
        assertEquals("|", keywords.examplesTableHeaderSeparator());
        assertEquals("|", keywords.examplesTableValueSeparator());
        assertEquals("Given", keywords.given());
        assertEquals("When", keywords.when());
        assertEquals("Then", keywords.then());
        assertEquals("And", keywords.and());
        assertEquals("!--", keywords.ignorable());
        assertEquals("PENDING", keywords.pending());
        assertEquals("NOT PERFORMED", keywords.notPerformed());
        assertEquals("FAILED", keywords.failed());
        assertEquals("DRY RUN", keywords.dryRun());
    }

    @Test(expected = KeywordNotFoundException.class)
    public void shouldFailIfSomeKeywordIsMissingInMapConstructor() throws IOException {
        new Keywords(new HashMap<String, String>(), new StringCoder());
    }

}
