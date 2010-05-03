package org.jbehave.core.parser;

import org.jbehave.core.parser.stories.MyPendingStory;
import org.junit.Test;

import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RelativeFileLoadingBehaviour {

    @Test
    public void canLoadStoryContent() {
        // Given
        String storyPath = "org/jbehave/core/parser/stories/MyPendingStory.txt";
        String storyAsString = "Given my step";

        // When
        StoryLoader loader = new LoadFromRelativeFile(MyPendingStory.class, "../../src/behaviour/java");
        loader.loadStoryAsText(storyPath);
       
    }


}