package org.jbehave.core.parser;

import org.jbehave.core.parser.stories.MyPendingStory;
import org.junit.Test;

public class RelativeFileLoadingBehaviour {

    @Test
    public void canLoadStoryContent() {
        // Given
        String storyPath = "org/jbehave/core/parser/stories/MyPendingStory.txt";

        // When
        StoryLoader loader = new LoadFromRelativeFile(MyPendingStory.class, "../../src/behaviour/java");
        loader.loadStoryAsText(storyPath);
       
    }


}