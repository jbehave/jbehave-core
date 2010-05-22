package org.jbehave.core.io;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.errors.InvalidStoryResourceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Loads the content from a place that is relative and
 * predictable to the compiled scenario class.
 * <p/>
 * See MAVEN_TEST_DIR, which implies a traversal out of 'target/test-classes'
 */
public class LoadFromRelativeFile implements StoryLoader {

    private final String traversal;
    private final URL location;
    private static final String MAVEN_TEST_DIR = "../../src/test/java";

    public LoadFromRelativeFile(Class<?> storyClass, String traversal) {
        this.traversal = traversal;
        this.location = locationFor(storyClass);
    }
    
    public LoadFromRelativeFile(Class<?> storyClass) {
        this(storyClass, MAVEN_TEST_DIR);
    }

    protected URL locationFor(Class<?> storyClass) {
        return storyClass.getProtectionDomain().getCodeSource().getLocation();
    }

    public String loadStoryAsText(String storyPath) {
        String fileLocation = null;
        try {
            fileLocation = new File(location.getFile()).getCanonicalPath() + "/";
            fileLocation = fileLocation + traversal + "/" + storyPath;
            fileLocation = fileLocation.replace("/", File.separator); // Windows and Unix
            File file = new File(fileLocation);
            return IOUtils.toString(new FileInputStream(file));            
        } catch (IOException e) {
            throw new InvalidStoryResourceException("Story path '" + storyPath + "' not found. Was looking in '" + fileLocation + "'", e);
        }

    }

}