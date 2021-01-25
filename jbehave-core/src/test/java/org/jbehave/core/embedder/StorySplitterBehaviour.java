package org.jbehave.core.embedder;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
        assertThat(splitStories.size(), is(2));
        Story first = splitStories.get(0);
        assertThat(first.getPath(), is(String.format(STORY_PATH_TEMPLATE, INDEX_ZERO)));
        assertThat(first.getName(), is(String.format(STORY_NAME_TEMPLATE, INDEX_ZERO)));
        ExamplesTable firstExamplesTable = first.getLifecycle().getExamplesTable();
        assertThat(firstExamplesTable.asString(), is(EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_FIRST_LINE));
        Story second = splitStories.get(1);
        ExamplesTable secondExampleTable = second.getLifecycle().getExamplesTable();
        assertThat(secondExampleTable.asString(), is(EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_SECOND_LINE));
        assertThat(second.getPath(), is(String.format(STORY_PATH_TEMPLATE, INDEX_FIRST)));
        assertThat(second.getName(), is(String.format(STORY_NAME_TEMPLATE, INDEX_FIRST)));
    }

    @Test
    public void testSplitStoriesWithSingleStoryTable() {
        List<Story> originStories = getOriginStories(EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_SECOND_LINE);
        List<Story> splitStories = StorySplitter.splitStories(originStories);
        assertThat(splitStories.size(), is(1));
        Story storyFirst = splitStories.get(0);
        assertThat(storyFirst.getPath(), is(String.format(STORY_PATH_TEMPLATE, EMPTY)));
        ExamplesTable examplesTableOfFirstStory = storyFirst.getLifecycle().getExamplesTable();
        assertThat(examplesTableOfFirstStory.asString(), is(EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_SECOND_LINE));
    }

    @Test
    public void testSplitStoriesWithEmptyStoryTable() {
        List<Story> originStories = getOriginStories(EXAMPLE_TABLE_HEADER);
        List<Story> splitStories = StorySplitter.splitStories(originStories);
        assertThat(splitStories.size(), is(1));
        assertThat(splitStories.get(0), is(originStories.get(0)));
    }

    @Test
    public void testSplitStoriesWithEmptyLifecycle() {
        Story originStory = new Story(EMPTY_STORY, EMPTY, Lifecycle.EMPTY);
        List<Story> originStories = new ArrayList<Story>();
        originStories.add(originStory);
        List<Story> splitStories = StorySplitter.splitStories(originStories);
        assertThat(splitStories.size(), is(1));
        assertThat(splitStories.get(0), is(originStories.get(0)));
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
