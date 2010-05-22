package org.jbehave.core.io;

import org.jbehave.core.io.LoadFromRelativeFile;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.stories.MyPendingStory;
import org.junit.Test;

public class RelativeFileLoadingBehaviour {

    @Test
    public void canLoadStoryContent() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/MyPendingStory.txt";

        // When
        StoryLoader loader = new LoadFromRelativeFile(MyPendingStory.class, "../../src/behaviour/java");
        loader.loadStoryAsText(storyPath);
       
    }


}