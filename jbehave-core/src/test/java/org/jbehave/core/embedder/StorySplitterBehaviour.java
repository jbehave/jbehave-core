package org.jbehave.core.embedder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;

class StorySplitterBehaviour {

    private static final String EMPTY = "";
    private static final Story EMPTY_STORY = new Story();
    private static final String STORY_NAME_TEMPLATE = "my%s.story";
    private static final String INDEX_ZERO = " [0]";
    private static final String INDEX_FIRST = " [1]";
    private static final String STORY_PATH_TEMPLATE = "path/to/" + STORY_NAME_TEMPLATE;
    private static final String EXAMPLE_TABLE_HEADER = "|one|two|three|\n";
    private static final String EXAMPLE_TABLE_FIRST_LINE = "|11|12|13|\n";
    private static final String EXAMPLE_TABLE_SECOND_LINE = "|21|22|23|\n";

    private StorySplitter createStorySplitter() {
        return createStorySplitter(" [0]");
    }

    private static StorySplitter createStorySplitter(String indexFormat) {
        return new StorySplitter(new DecimalFormat(indexFormat));
    }

    @Test
    void testSplitStoriesWithStoryTable() {
        List<Story> originStories = getOriginStories((EXAMPLE_TABLE_HEADER
                + EXAMPLE_TABLE_FIRST_LINE + EXAMPLE_TABLE_SECOND_LINE));
        List<Story> splitStories = createStorySplitter().splitStories(originStories);
        assertThat(splitStories.size(), is(2));
        Story first = splitStories.get(0);
        assertAll(
                () -> assertThat(first.getPath(), is(String.format(STORY_PATH_TEMPLATE, INDEX_ZERO))),
                () -> assertThat(first.getName(), is(String.format(STORY_NAME_TEMPLATE, INDEX_ZERO))),
                () -> assertThat(first.getLifecycle().getExamplesTable().asString(),
                        is(EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_FIRST_LINE))
        );
        Story second = splitStories.get(1);
        assertAll(
                () -> assertThat(second.getPath(), is(String.format(STORY_PATH_TEMPLATE, INDEX_FIRST))),
                () -> assertThat(second.getName(), is(String.format(STORY_NAME_TEMPLATE, INDEX_FIRST))),
                () -> assertThat(second.getLifecycle().getExamplesTable().asString(),
                        is(EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_SECOND_LINE))
        );
    }

    @Test
    void testSplitStoriesWithStoryTableAndUsingCustomStoryIndexFormat() {
        int storiesSize = 11;
        StringBuilder table = new StringBuilder(EXAMPLE_TABLE_HEADER);
        for (int i = 0; i < storiesSize; i++) {
            table.append(EXAMPLE_TABLE_FIRST_LINE);
        }
        List<Story> originStories = getOriginStories(table.toString());
        List<Story> splitStories = createStorySplitter(" [00]").splitStories(originStories);
        assertThat(splitStories.size(), is(storiesSize));
        assertAll(
                () -> assertThat(splitStories.get(0).getName(), is(String.format(STORY_NAME_TEMPLATE, " [00]"))),
                () -> assertThat(splitStories.get(1).getName(), is(String.format(STORY_NAME_TEMPLATE, " [01]"))),
                () -> assertThat(splitStories.get(10).getName(), is(String.format(STORY_NAME_TEMPLATE, " [10]")))
        );
    }

    @Test
    void testSplitStoriesWithSingleStoryTable() {
        List<Story> originStories = getOriginStories(EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_SECOND_LINE);
        List<Story> splitStories = createStorySplitter().splitStories(originStories);
        assertThat(splitStories.size(), is(1));
        Story storyFirst = splitStories.get(0);
        assertThat(storyFirst.getPath(), is(String.format(STORY_PATH_TEMPLATE, EMPTY)));
        ExamplesTable examplesTableOfFirstStory = storyFirst.getLifecycle().getExamplesTable();
        assertThat(examplesTableOfFirstStory.asString(), is(EXAMPLE_TABLE_HEADER + EXAMPLE_TABLE_SECOND_LINE));
    }

    @Test
    void testSplitStoriesWithEmptyStoryTable() {
        List<Story> originStories = getOriginStories(EXAMPLE_TABLE_HEADER);
        List<Story> splitStories = createStorySplitter().splitStories(originStories);
        assertThat(splitStories, is(originStories));
    }

    @Test
    void testSplitStoriesWithEmptyLifecycle() {
        Story originStory = new Story(EMPTY_STORY, EMPTY, Lifecycle.EMPTY);
        List<Story> originStories = Collections.singletonList(originStory);
        List<Story> splitStories = createStorySplitter().splitStories(originStories);
        assertThat(splitStories, is(originStories));
    }

    private List<Story> getOriginStories(String table) {
        ExamplesTable examplesTable = new ExamplesTable(table);
        Lifecycle lifecycle = new Lifecycle(examplesTable);
        Story originStory = new Story(EMPTY_STORY, String.format(STORY_PATH_TEMPLATE, EMPTY), lifecycle);
        return Collections.singletonList(originStory);
    }
}
