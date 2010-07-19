package org.jbehave.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Defaults to working from classes compiled to Maven-style
 * 'target/test-classes', with story source in 'src/test/java'
 * 
 * LoadFromRelativeFile loader = new
 * LoadFromRelativeFile(codeLocationFromClass(YourStory.class));
 * 
 * To work with something other than the default story locations, you will have
 * to specify them in the varargs constructor.
 * 
 * LoadFromRelativeFile loader = new
 * LoadFromRelativeFile(codeLocationFromClass(YourStory.class),
 * mavenModuleTestStoryFilePath("src/behaviour/java"),
 * intellijProjectTestStoryFilePath("src/behaviour/java"));
 * 
 * Convenience methods : {@link LoadFromRelativeFile#mavenModuleStoryFilePath},
 * {@link LoadFromRelativeFile#mavenModuleTestStoryFilePath}
 * {@link LoadFromRelativeFile#intellijProjectStoryFilePath}
 * {@link LoadFromRelativeFile#intellijProjectTestStoryFilePath}
 * 
 * See also {@link StoryLocation#codeLocationFromClass}
 * 
 */
public class LoadFromRelativeFile implements StoryLoader {

    private final StoryFilePath[] traversals;
    private final URL location;

    public LoadFromRelativeFile(URL location) {
        this(location, mavenModuleStoryFilePath("src/test/java"));
    }

    public LoadFromRelativeFile(URL location, StoryFilePath... traversals) {
        this.traversals = traversals;
        this.location = location;
    }

    public String loadStoryAsText(String storyPath) {
        List<String> traversalPaths = new ArrayList<String>();
        for (StoryFilePath traversal : traversals) {
            try {
                String filePath = new File(location.getFile()).getCanonicalPath() + "/";
                filePath = filePath.replace(traversal.toRemove, traversal.relativePath) + "/" + storyPath;
                File file = new File(filePath);
                if (file.exists()) {
                    return IOUtils.toString(new FileInputStream(file));
                } else {
                    traversalPaths.add(filePath);
                }
            } catch (IOException e) {
                throw new InvalidStoryResource("Story path '" + storyPath + "' not found.", e);
            }
        }
        throw new InvalidStoryResource("Story path '" + storyPath + "' not found while looking in '" + traversalPaths
                + "'", null);

    }

    /**
     * For use the the varargs constructor of {@link LoadFromRelativeFile}, to
     * allow a range of possibilities for locating Story file paths
     */
    public static class StoryFilePath {
        private final String toRemove;
        private final String relativePath;

        public StoryFilePath(String toRemove, String relativePath) {
            this.toRemove = toRemove.replace('\\', '/');
            this.relativePath = relativePath.replace('\\', '/');
        }
    }

    /**
     * Maven by default, has its PRODUCTION classes in target/classes. This
     * story file path is relative to that.
     * 
     * @param relativePath
     *            the path to the stories' base-dir inside the module
     * @return the resulting StoryFilePath
     */
    public static StoryFilePath mavenModuleStoryFilePath(String relativePath) {
        return new StoryFilePath("target/classes", relativePath);
    }

    /**
     * Maven by default, has its TEST classes in target/test-classes. This story
     * file path is relative to that.
     * 
     * @param relativePath
     *            the path to the stories' base-dir inside the module
     * @return the resulting StoryFilePath
     */
    public static StoryFilePath mavenModuleTestStoryFilePath(String relativePath) {
        return new StoryFilePath("target/test-classes", relativePath);
    }

    /**
     * Intellij by default, has its PRODUCTION classes in classes/production.
     * This story file path is relative to that.
     * 
     * @param relativePath
     *            the path to the stories' base-dir inside the module
     * @return the resulting StoryFilePath
     */
    public static StoryFilePath intellijProjectStoryFilePath(String relativePath) {
        return new StoryFilePath("classes/production", relativePath);
    }

    /**
     * Intellij by default, has its TEST classes in classes/test. This story
     * file path is relative to that.
     * 
     * @param relativePath
     *            the path to the stories' base-dir inside the module
     * @return the resulting StoryFilePath
     */
    public static StoryFilePath intellijProjectTestStoryFilePath(String relativePath) {
        return new StoryFilePath("classes/test", relativePath);
    }

}