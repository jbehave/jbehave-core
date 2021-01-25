package org.jbehave.core.configuration;

import org.jbehave.core.configuration.Keywords.KeywordNotFound;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class KeywordsBehaviour {

    @Test
    public void shouldHaveAllKeywordsSetByDefault() {
        Keywords keywords = new Keywords();
        assertThat(keywords.narrative(), equalTo("Narrative:"));
        assertThat(keywords.scenario(), equalTo("Scenario:"));
        assertThat(keywords.givenStories(), equalTo("GivenStories:"));
        assertThat(keywords.examplesTable(), equalTo("Examples:"));
        assertThat(keywords.examplesTableRow(), equalTo("Example:"));
        assertThat(keywords.examplesTableHeaderSeparator(), equalTo("|"));
        assertThat(keywords.examplesTableValueSeparator(), equalTo("|"));
        assertThat(keywords.examplesTableIgnorableSeparator(), equalTo("|--"));
        assertThat(keywords.given(), equalTo("Given"));
        assertThat(keywords.when(), equalTo("When"));
        assertThat(keywords.then(), equalTo("Then"));
        assertThat(keywords.and(), equalTo("And"));
        assertThat(keywords.ignorable(), equalTo("!--"));
        assertThat(keywords.pending(), equalTo("PENDING"));
        assertThat(keywords.notPerformed(), equalTo("NOT PERFORMED"));
        assertThat(keywords.failed(), equalTo("FAILED"));
        assertThat(keywords.dryRun(), equalTo("DRY RUN"));
    }

    @Test
    public void shouldFailIfSomeKeywordIsMissingInMapConstructor() {
        try {
            new Keywords(new HashMap<String, String>());
        } catch (Exception e) {
            assertThat(e, is(instanceOf(KeywordNotFound.class)));
        }
    }

}
