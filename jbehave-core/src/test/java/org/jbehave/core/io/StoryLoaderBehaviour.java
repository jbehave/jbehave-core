package org.jbehave.core.io;

import org.hamcrest.Matchers;
import org.jbehave.core.io.LoadFromRelativeFile.*;
import org.jbehave.core.io.stories.MyPendingStory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.io.LoadFromRelativeFile.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoryLoaderBehaviour {

    @Test
    void shouldLoadStoryFromClasspath() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/my_pending_story";
        String storyAsText = "Given my step";

        // When
        StoryLoader loader = new LoadFromClasspath();
        String loadedStoryAsText = loader.loadStoryAsText(storyPath);
        
        // Then
        assertThat(loadedStoryAsText, equalTo(storyAsText));

    }

    @Test
    void shouldNotLoadStoryFromClasspathIfNotFound() {
        StoryLoader loader = new LoadFromClasspath(StoryLoaderBehaviour.class);
        assertThrows(StoryResourceNotFound.class, () -> loader.loadStoryAsText("unexistent.story"));
    }

    @Test
    void shouldNotLoadStoryFromClasspathIfClassloaderNotValid() {
        StoryLoader loader = new LoadFromClasspath(new InvalidClassLoader());
        assertThat(loader.toString(), Matchers.containsString(InvalidClassLoader.class.getName()));
        assertThrows(InvalidStoryResource.class, () -> loader.loadStoryAsText("unexistent.story"));
    }

    static class InvalidClassLoader extends ClassLoader {

        @Override
        public InputStream getResourceAsStream(String name) {
            return new InputStream() {

                @Override
                public int available() {
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
    void shouldLoadStoryFromURL() {
        String storyPath = CodeLocations.codeLocationFromClass(this.getClass()) + "org/jbehave/core/io/stories/my_pending_story";
        String storyAsText = "Given my step";
 
        // When
        StoryLoader loader = new LoadFromURL();
        String loadedStoryAsText = loader.loadStoryAsText(storyPath);
        
        // Then
        assertThat(loadedStoryAsText, equalTo(storyAsText));
    }

    @Test
    void shouldNotLoadStoryFromURLIfNotFound() {
        String storyPath = CodeLocations.codeLocationFromClass(this.getClass()) + "inexistent_story";

        // When
        StoryLoader loader = new LoadFromURL();
        assertThrows(InvalidStoryResource.class, () -> loader.loadStoryAsText(storyPath));

        // Then fail as expected
    }

    @Test
    void shouldLoadStoryFromRelativeFilePaths() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/MyPendingStory.txt";
        String storyAsText = "Given my step";

        // When
        StoryLoader loader = new LoadFromRelativeFile(CodeLocations.codeLocationFromClass(MyPendingStory.class),
                mavenModuleStoryFilePath("src/main/java"),
                mavenModuleTestStoryFilePath("src/test/java"),
                intellijProjectStoryFilePath("src/main/java"),
                intellijProjectTestStoryFilePath("src/test/java"));
        
        // Then
        assertThat(loader.loadStoryAsText(storyPath), equalTo(storyAsText));

    }

    @Test
    void shouldLoadStoryFromDefaultRelativeFilePaths() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/MyPendingStory.txt";
        String storyAsText = "Given my step";

        // When
        StoryLoader loader = new LoadFromRelativeFile(CodeLocations.codeLocationFromClass(MyPendingStory.class));

        // Then
        assertThat(loader.loadStoryAsText(storyPath), equalTo(storyAsText));

    }
    
    @Test
    void shouldNotLoadStoryFromRelativeFileWhenNoPathsAreProvided() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/MyPendingStory.txt";

        // When
        StoryLoader loader = new LoadFromRelativeFile(CodeLocations.codeLocationFromClass(MyPendingStory.class), new StoryFilePath[]{});

        // Then fail as expected
        assertThrows(StoryResourceNotFound.class, () -> loader.loadStoryAsText(storyPath));
    }
    
    @Test
    void shouldNotLoadStoryFromRelativeFileWhenNotFound() {
        // Given
        String storyPath = "org/jbehave/core/io/stories/MyInexistentStory";

        // When
        StoryLoader loader = new LoadFromRelativeFile(CodeLocations.codeLocationFromClass(MyPendingStory.class));

        // Then fail as expected
        assertThrows(StoryResourceNotFound.class, () -> loader.loadStoryAsText(storyPath));
    }

    @Test
    void shouldNotLoadStoryFromRelativeFileWhenPathInvalid() {
        // Given
        String storyPath = null;

        // When
        LoadFromRelativeFile loader = new LoadFromRelativeFile(CodeLocations.codeLocationFromClass(MyPendingStory.class));

        // Then fail as expected
        assertThrows(InvalidStoryResource.class, () -> loader.loadContent(storyPath));
    }

    @Test
    void shouldLoadStoryFromRelativeFilePathsWithSpace() throws MalformedURLException, URISyntaxException {
        shouldWorkForPath("/org/jbehave/core/io/stories/foldername has spaces");
    }

    @Test
    void shouldLoadStoryFromRelativeFilePathsWithBizarreName() throws MalformedURLException, URISyntaxException {
        shouldWorkForPath("/org/jbehave/core/io/stories/[mage_hg]");
    }

    private void shouldWorkForPath(String path) throws URISyntaxException, MalformedURLException {
        // Given
        String storyPath = "MyPendingStory.txt";
        String storyAsText = "Given my step";
        URL url = CodeLocations.codeLocationFromClass(MyPendingStory.class);
        File folderWithSpacesInName = new File(url.toURI().getPath() + path);
        URL urlThatHasEscapedSpaces = folderWithSpacesInName.toURI().toURL();
        assertThat(folderWithSpacesInName.exists(), is(true));

        // When
        StoryLoader loader = new LoadFromRelativeFile(urlThatHasEscapedSpaces);

        // Then
        assertThat(loader.loadStoryAsText(storyPath), equalTo(storyAsText));
    }


}
