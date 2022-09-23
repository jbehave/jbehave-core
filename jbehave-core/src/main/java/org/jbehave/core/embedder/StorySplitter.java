package org.jbehave.core.embedder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Story;

/**
 * Splits story into the list of stories basing on Lifecycle ExamplesTable and the provided story index format.
 */
public class StorySplitter {

    private final NumberFormat storyIndexFormat;

    public StorySplitter(NumberFormat storyIndexFormat) {
        this.storyIndexFormat = storyIndexFormat;
    }

    public List<Story> splitStories(List<Story> stories) {
        List<Story> splitStories = new ArrayList<>();
        for (Story story: stories) {
            if (story.getLifecycle().getExamplesTable().getRowCount() > 1) {
                splitStories.addAll(splitStory(story));
            } else {
                splitStories.add(story);
            }
        }
        return splitStories;
    }

    private List<Story> splitStory(Story story) {
        List<Story> splitStories = new ArrayList<>();
        List<Map<String, String>> rows = story.getLifecycle().getExamplesTable().getRows();
        for (int i = 0; i < rows.size(); i++) {
            ExamplesTable examplesTable = ExamplesTable.empty().withRows(Collections.singletonList(rows.get(i)));
            Lifecycle originLifecycle = story.getLifecycle();
            Lifecycle lifecycle = new Lifecycle(examplesTable, originLifecycle.getBefore(), originLifecycle.getAfter());

            String path = story.getPath().replace(".story", storyIndexFormat.format(i) + ".story");
            Story splitStory = new Story(story, path, lifecycle);
            splitStories.add(splitStory);
        }
        return splitStories;
    }
}
