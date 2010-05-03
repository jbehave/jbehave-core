package org.jbehave.core.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.jbehave.core.model.Keywords.KeywordNotFoundException;
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
    }

    @Test(expected=KeywordNotFoundException.class)
    public void shouldFailIfSomeKeywordIsMissingInMapConstructor() throws IOException {
        new Keywords(new HashMap<String, String>());
    }
    
}
