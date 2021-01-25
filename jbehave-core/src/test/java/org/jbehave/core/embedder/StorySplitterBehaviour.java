package org.jbehave.core.embedder;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Story;
import org.junit.Test;

public class StorySplitterBehaviour {
    private static final String EMPTY = "";
    private static final Story EMPTY_STORY = new Story();
    private static final String STORY_NAME_TEMPLATE = "my%s.story";
    private static final String INDEX_ZERO = " [0]";
    private static final String INDEX_FIRST = " [1]";
    private static final String STORY_PATH_TEMPLATE = "path/to/" + STORY_NAME_TEMPLATE;
    private static final String EXAMPLE_TABLE_HEADER = "|one|two|three|\n";
    private static final String EXAMPLE_TABLE_FIRST_LINE = "|11|12|13|\n";
    private static final String EXAMPLE_TABLE_SECOND_LINE = "|21|22|23|\n";

    @Test
    public void testSplitStoriesWithStoryTable() {
        List<Story> originStories = getOriginStories((EXAMPLE_TABLE_HEADER
                + EXAMPLE_TABLE_FIRST_LINE + EXAMPLE_TABLE_SECOND_LINE));
        List<Story> splitStories = StorySplitter.splitStories(originStories);
        assertEquals(2, splitStories.size());
        Story storyFirst = splitStories.get(0);
        Story storySecond = splitStories.get(1);
        assertEquals(String.format(STORY_PATH_TEMPLATE, INDEX_ZERO), storyFirst.getPath());
        assertEquals(String.format(STORY_NAME_TEMPLATE, INDEX_ZERO), storyFirst.getName());
        ExamplesTable examplesTableOfFirstStory = storyFirst.getLifecycle().getExamplesTable();
        ExamplesTable examplesTableOfSecondStory = storySecond.getLifecycle().getExamplesTable();
        assertEquals(examplesTableOfFirstStory.asString(), EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_FIRST_LINE);
        assertEquals(examplesTableOfSecondStory.asString(), EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_SECOND_LINE);
        assertEquals(String.format(STORY_PATH_TEMPLATE, INDEX_FIRST), storySecond.getPath());
        assertEquals(String.format(STORY_NAME_TEMPLATE, INDEX_FIRST), storySecond.getName());
    }

    @Test
    public void testSplitStoriesWithSingleStoryTable() {
        List<Story> originStories = getOriginStories(EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_SECOND_LINE);
        List<Story> splitStories = StorySplitter.splitStories(originStories);
        assertEquals(1, splitStories.size());
        Story storyFirst = splitStories.get(0);
        assertEquals(String.format(STORY_PATH_TEMPLATE, EMPTY), storyFirst.getPath());
        ExamplesTable examplesTableOfFirstStory = storyFirst.getLifecycle().getExamplesTable();
        assertEquals(examplesTableOfFirstStory.asString(), EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_SECOND_LINE);
    }

    @Test
    public void testSplitStoriesWithEmptyStoryTable() {
        List<Story> originStories = getOriginStories(EXAMPLE_TABLE_HEADER);
        List<Story> splitStories = StorySplitter.splitStories(originStories);
        assertEquals(1, splitStories.size());
        assertEquals(originStories.get(0), splitStories.get(0));
    }

    @Test
    public void testSplitStoriesWithEmptyLifecycle() {
        Story originStory = new Story(EMPTY_STORY, EMPTY, Lifecycle.EMPTY);
        List<Story> originStories = new ArrayList<Story>();
        originStories.add(originStory);
        List<Story> splitStories = StorySplitter.splitStories(originStories);
        assertEquals(1, splitStories.size());
        assertEquals(originStories.get(0), splitStories.get(0));
    }

    private List<Story> getOriginStories(String table) {
        ExamplesTable examplesTable = new ExamplesTable(table);
        Lifecycle lifecycle = new Lifecycle(examplesTable);
        Story originStory = new Story(EMPTY_STORY, String.format(STORY_PATH_TEMPLATE, EMPTY), lifecycle);
        originStory.namedAs(String.format(STORY_NAME_TEMPLATE, EMPTY));
        List<Story> originStories = new ArrayList<Story>();
        originStories.add(originStory);
        return originStories;
    }
}
