package org.jbehave.core.io;

import org.jbehave.core.errors.InvalidStoryResourceException;
import org.jbehave.core.errors.StoryNotFoundException;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.StoryLocation;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class StoryLoaderBehaviour {


    @Test
    public void canLoadStoryFromClasspath() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/my_pending_story";
        String storyAsString = "Given my step";

        // When
        StoryLoader loader = new LoadFromClasspath();
        assertThat(loader.loadStoryAsText(storyPath), equalTo(storyAsString));

    }

    @Test(expected = StoryNotFoundException.class)
    public void cannotDefineStoryWithClasspathLoadingForInexistentResource() {

        StoryLoader loader = new LoadFromClasspath();
        loader.loadStoryAsText("inexistent.story");

    }

    @Test(expected = InvalidStoryResourceException.class)
    public void cannotDefineStoryWithClasspathLoadingForInvalidResource() {

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
        String storyAsString = "Given my step";
 
        // When
        StoryLoader loader = new LoadFromURL();
        assertThat(loader.loadStoryAsText(storyPath), equalTo(storyAsString));
    }

    @Test(expected = InvalidStoryResourceException.class)
    public void cannotDefineStoryWithURLLoadingForInexistentResource() {
        // Given
        String codeLocation = new StoryLocation("", this.getClass()).getCodeLocation().getFile();
        String storyPath = "file:" + codeLocation + "inexistent_story";

        // When
        StoryLoader loader = new LoadFromURL();
        loader.loadStoryAsText(storyPath);
        
        // Then
        // fail as expected

    }

    @Test(expected = InvalidStoryResourceException.class)
    public void cannotDefineStoryWithURLLoadingForInvalidURL() {
        // Given
        String codeLocation = new StoryLocation("", this.getClass()).getCodeLocation().getFile();
        String storyPath = "file:" + codeLocation + "inexistent_story";

        // When
        StoryLoader loader = new LoadFromURL();
        loader.loadStoryAsText(storyPath);

        // Then
        // fail as expected
    }

}