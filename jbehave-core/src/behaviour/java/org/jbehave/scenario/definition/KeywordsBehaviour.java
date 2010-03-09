package org.jbehave.scenario.definition;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.jbehave.scenario.definition.KeyWords.InsufficientKeywordsException;
import org.jbehave.scenario.definition.KeyWords.KeywordNotFoundException;
import org.junit.Test;

public class KeywordsBehaviour {

    @Test
    public void shouldHaveAllKeywordsSetByDefault() throws IOException {
        KeyWords keywords = new KeyWords();
        assertEquals("Narrative:", keywords.narrative());
        assertEquals("Scenario:", keywords.scenario());
        assertEquals("GivenScenarios:", keywords.givenScenarios());
        assertEquals("Examples:", keywords.examplesTable());
        assertEquals("Example:", keywords.examplesTableRow());
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
        new KeyWords(new HashMap<String, String>());
    }

    @Test(expected=InsufficientKeywordsException.class)
    public void shouldFailIfSomeKeywordIsMissingInVarargConstructor() throws IOException {
        new KeyWords("scenario", "givenScenario", "examples", "given", "when", "then", "and", "!--", "narrative");
    }

}
