package org.jbehave.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads story resources from relative file paths that are
 * traversal to a given location.
 * 
 * StoryLoader loader = new
 * LoadFromRelativeFile(codeLocationFromClass(YourStory.class));
 * 
 * By default, it uses traversal directory
 * 'target/test-classes' with source dir in 'src/test/java'.
 * 
 * Other traversal locations can be specified via the varargs constructor:
 * 
 * StoryLoader loader = new
 * LoadFromRelativeFile(codeLocationFromClass(YourStory.class),
 * mavenModuleTestStoryFilePath("src/test/java"),
 * intellijProjectTestStoryFilePath("src/test/java"));
 * 
 * Convenience methods : {@link LoadFromRelativeFile#mavenModuleStoryFilePath},
 * {@link LoadFromRelativeFile#mavenModuleTestStoryFilePath}
 * {@link LoadFromRelativeFile#intellijProjectStoryFilePath}
 * {@link LoadFromRelativeFile#intellijProjectTestStoryFilePath}
 * 
 * @see {@link CodeLocations#codeLocationFromClass(Class)}
 * 
 */
public class LoadFromRelativeFile implements ResourceLoader, StoryLoader {

    private final StoryFilePath[] traversals;
    private final URL location;

    public LoadFromRelativeFile(URL location) {
        this(location, mavenModuleStoryFilePath("src/test/java"));
    }

    public LoadFromRelativeFile(URL location, StoryFilePath... traversals) {
        this.traversals = traversals;
        this.location = location;
    }
    
    public String loadResourceAsText(String resourcePath) {
        List<String> traversalPaths = new ArrayList<>();
        String locationPath = normalise(new File(CodeLocations.getPathFromURL(location)).getAbsolutePath());
        for (StoryFilePath traversal : traversals) {
            String filePath = locationPath.replace(traversal.toRemove, traversal.relativePath) + "/" + resourcePath;
            File file = new File(filePath);
            if (file.exists()) {
                return loadContent(filePath);
            } else {
                traversalPaths.add(filePath);
            }
        }
        throw new StoryResourceNotFound(resourcePath, traversalPaths);
    }

    public String loadStoryAsText(String storyPath) {
        List<String> traversalPaths = new ArrayList<>();
        String locationPath = new File(CodeLocations.getPathFromURL(location)).getAbsolutePath();
        for (StoryFilePath traversal : traversals) {
            String filePath = locationPath.replace(traversal.toRemove, traversal.relativePath) + "/" + storyPath;
            File file = new File(filePath);
            if (file.exists()) {
                return loadContent(filePath);
            } else {
                traversalPaths.add(filePath);
            }
        }
        throw new StoryResourceNotFound(storyPath, traversalPaths);
    }

    protected String loadContent(String path) {
        try {
            return IOUtils.toString(new FileInputStream(new File(path)), true);
        } catch (Exception e) {
            throw new InvalidStoryResource(path, e);
        }
    }

    private static String normalise(String path) {
        return path.replace('\\', '/');
    }

    /**
     * For use the the varargs constructor of {@link LoadFromRelativeFile}, to
     * allow a range of possibilities for locating Story file paths
     */
    public static class StoryFilePath {
        private final String toRemove;
        private final String relativePath;

        public StoryFilePath(String toRemove, String relativePath) {
            this.toRemove = normalise(toRemove);
            this.relativePath = normalise(relativePath);
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
