package org.jbehave.core.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Valery_Yatsynovich
 */
public class GivenStoriesBehaviour {

    private static final String GIVEN_STORY_PATH1 = "/path/to/given1.story";
    private static final String GIVEN_STORY_PATH2 = "/path/to/given2.story";

    @Test
    public void shouldParseSimpleGivenStories() {
        GivenStories givenStories = new GivenStories(GIVEN_STORY_PATH1 + "," + GIVEN_STORY_PATH2);

        assertEquals(Arrays.asList(GIVEN_STORY_PATH1, GIVEN_STORY_PATH2), givenStories.getPaths());
    }

    @Test
    public void shouldParseGivenStoriesWithTrailingComma() {
        GivenStories givenStories = new GivenStories(GIVEN_STORY_PATH1 + ",");

        assertEquals(Arrays.asList(GIVEN_STORY_PATH1), givenStories.getPaths());
    }

    @Test
    public void shouldParseGivenStoriesWithTrailingCommaAndSpaces() {
        GivenStories givenStories = new GivenStories(GIVEN_STORY_PATH1 + ",  ");

        assertEquals(Arrays.asList(GIVEN_STORY_PATH1), givenStories.getPaths());
    }

    @Test
    public void shouldParseGivenStoriesWithEmptyStoryPathInTheMiddle() {
        GivenStories givenStories = new GivenStories(GIVEN_STORY_PATH1 + ",  ," + GIVEN_STORY_PATH2);

        assertEquals(Arrays.asList(GIVEN_STORY_PATH1, GIVEN_STORY_PATH2), givenStories.getPaths());
    }

    @Test
    public void shouldParseGivenStoriesWithLeadingComma() {
        GivenStories givenStories = new GivenStories("," + GIVEN_STORY_PATH1);

        assertEquals(Arrays.asList(GIVEN_STORY_PATH1), givenStories.getPaths());
    }

    @Test
    public void shouldParseGivenStoriesWithLeadingCommaAndSpaces() {
        GivenStories givenStories = new GivenStories("  ," + GIVEN_STORY_PATH1);

        assertEquals(Arrays.asList(GIVEN_STORY_PATH1), givenStories.getPaths());
    }

}
