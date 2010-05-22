package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.jbehave.core.io.stories.MyPendingStory;
import org.junit.Test;

public class RelativeFileLoadingBehaviour {

    @Test
    public void shouldLoadStoryFromFileWithRelativeFilePath() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/MyPendingStory.txt";
		String storyAsText = "Given my step";

        // When
        StoryLoader loader = new LoadFromRelativeFile(MyPendingStory.class, "../../src/behaviour/java");
        String loadedStoryAsText = loader.loadStoryAsText(storyPath);
        
        // Then
		assertThat(loadedStoryAsText, equalTo(storyAsText));
       
    }


}