package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class StoryLoaderBehaviour {

    @Test
    public void shouldLoadStoryFromClasspath() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/my_pending_story";
        String storyAsText = "Given my step";

        // When
        StoryLoader loader = new LoadFromClasspath();
        String loadedStoryAsText = loader.loadStoryAsText(storyPath);
        
        // Then
		assertThat(loadedStoryAsText, equalTo(storyAsText));

    }

    @Test(expected = StoryResourceNotFound.class)
    public void shouldNotLoadStoryFromClasspathIfNotFound() {

        StoryLoader loader = new LoadFromClasspath();
        loader.loadStoryAsText("inexistent.story");

    }

    @Test(expected = InvalidStoryResource.class)
    public void shouldNotLoadStoryFropmClasspathIfClassloaderNotValid() {

        StoryLoader loader = new LoadFromClasspath(new InvalidClassLoader());
        loader.loadStoryAsText("inexistent.story");

    }

    static class InvalidClassLoader extends ClassLoader {

        @Override
        public InputStream getResourceAsStream(String name) {
            return new InputStream() {

                public int available() throws IOException {
                    return 1;
                }

                @Override
                public int read() throws IOException {
                    throw new IOException("invalid");
                }

            };
        }
    }


    @Test
    public void canDefineStoryWithURLLoading() {
        // Given
        String codeLocation = new StoryLocation("", this.getClass()).getCodeLocation().getFile();
        String storyPath = "file:" + codeLocation + "org/jbehave/core/io/stories/my_pending_story";
        String storyAsText = "Given my step";
 
        // When
        StoryLoader loader = new LoadFromURL();
        String loadedStoryAsText = loader.loadStoryAsText(storyPath);
        
        // Then
		assertThat(loadedStoryAsText, equalTo(storyAsText));
    }

    @Test(expected = InvalidStoryResource.class)
    public void shouldNotLoadStoryFromURLIfNotFound() {
        // Given
        String codeLocation = new StoryLocation("", this.getClass()).getCodeLocation().getFile();
        String storyPath = "file:" + codeLocation + "inexistent_story";

        // When
        StoryLoader loader = new LoadFromURL();
        loader.loadStoryAsText(storyPath);
        
        // Then fail as expected

    }
}