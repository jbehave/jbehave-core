package org.jbehave.core.embedder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Story;

/**
 * Splits story into the list of stories based on examples table configuration
 */
public class StorySplitter {
    public static List<Story> splitStories(List<Story> stories) {
        List<Story> splitStories = new ArrayList<Story>();
        for (Story story: stories) {
            if (story.getLifecycle().getExamplesTable().getRowCount() > 1) {
                splitStories.addAll(splitStory(story));
            } else {
                splitStories.add(story);
            }
        }
        return splitStories;
    }

    private static List<Story> splitStory(Story story) {
        List<Story> splitStories = new ArrayList<Story>();
        List<Map<String, String>> rows = story.getLifecycle().getExamplesTable().getRows();
        for (int i = 0; i < rows.size(); i++) {
            ExamplesTable examplesTable = ExamplesTable.empty().withRows(Collections.singletonList(rows.get(i)));
            Lifecycle originLifecycle = story.getLifecycle();
            Lifecycle lifecycle = new Lifecycle(examplesTable, originLifecycle.getBefore(), originLifecycle.getAfter());
            String originPath = story.getPath();
            Story splitStory = new Story(story, indexStory(originPath, i), lifecycle);
            splitStory.namedAs(indexStory(story.getName(), i));
            splitStories.add(splitStory);
        }
        return splitStories;
    }

    private static String indexStory(String story, int index) {
        return story.replace(".story", String.format(" [%d].story", index));
    }
}
