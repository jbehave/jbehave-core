package org.jbehave.core.io;

import org.jbehave.core.io.stories.MyPendingStory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jbehave.core.io.LoadFromRelativeFile.intellijProjectTestStoryFilePath;
import static org.jbehave.core.io.LoadFromRelativeFile.mavenModuleTestStoryFilePath;

public class RelativeFileLoadingBehaviour {

    @Test
    public void shouldLoadStoryFromFileWithRelativeFilePath() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/MyPendingStory.txt";
		String storyAsText = "Given my step";

        // When
        LoadFromRelativeFile loader = new LoadFromRelativeFile(MyPendingStory.class,
                mavenModuleTestStoryFilePath("src/behaviour/java"),
                intellijProjectTestStoryFilePath("src/behaviour/java"));
        String loadedStoryAsText = loader.loadStoryAsText(storyPath);
        
        // Then
		assertThat(loadedStoryAsText, equalTo(storyAsText));
       
    }


}