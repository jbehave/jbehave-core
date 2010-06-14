package org.jbehave.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * Loads story content from a directory traversal relative to the compiled story
 * class.
 * <p/>
 */
public class LoadFromRelativeFile implements StoryLoader {

	private final CompileOutput[] traversals;
	private final URL location;

	public LoadFromRelativeFile(Class<?> storyClass) {
		this(storyClass, mavenModuleCompileOutput("src/test/java"));
	}

	public LoadFromRelativeFile(Class<?> storyClass, CompileOutput... traversals) {
		this.traversals = traversals;
		this.location = locationFor(storyClass);
	}

	protected URL locationFor(Class<?> storyClass) {
		return storyClass.getProtectionDomain().getCodeSource().getLocation();
	}

	public String loadStoryAsText(String storyPath) {
        String badFileLocations = "";
        Throwable badFileLocationCause = null;
		String fileLocation = null;
        for (CompileOutput traversal : traversals) {
            try {
                fileLocation = new File(location.getFile()).getCanonicalPath()
                        + "/";
                fileLocation = fileLocation.replace(traversal.toRemove, "") + "/" + traversal.relativePath + "/" + storyPath;
                fileLocation = fileLocation.replace("/", File.separator);
                File file = new File(fileLocation);
                return IOUtils.toString(new FileInputStream(file));
            } catch (IOException e) {
                badFileLocationCause = e;
                if (badFileLocations.length() > 0) {
                    badFileLocations = badFileLocations + ", ";
                }
                badFileLocations = badFileLocations + fileLocation;
            }
        }
        throw new InvalidStoryResource("Story path '" + storyPath
                + "' not found while looking in '" + badFileLocations + "'", badFileLocationCause);

	}

    public static class CompileOutput {
        private final String toRemove;
        private final String relativePath;

        public CompileOutput(String toRemove, String relativePath) {
            this.toRemove = toRemove;
            this.relativePath = relativePath;
        }
    }

    public static CompileOutput mavenModuleCompileOutput(String relativePath) {
        return new CompileOutput("target/classes", relativePath);
    }

    public static CompileOutput mavenModuleTestCompileOutput(String relativePath) {
        return new CompileOutput("target/test-classes", relativePath);
    }

    public static CompileOutput intellijProjectCompileOutput(String relativePath) {
        return new CompileOutput("classes/production", relativePath);
    }

    public static CompileOutput intellijProjectTestCompileOutput(String relativePath) {
        return new CompileOutput("classes/test", relativePath);
    }

}