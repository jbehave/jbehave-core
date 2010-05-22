package org.jbehave.core.io;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.errors.InvalidStoryResourceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Loads story content from a directory traversal relative to the compiled story
 * class.
 * <p/>
 * Defaults to using {@link TEST_DIR}, which implies a traversal out of 'target/test-classes'
 */
public class LoadFromRelativeFile implements StoryLoader {

	private final String traversal;
	private final URL location;
	private static final String TEST_DIR = "../../src/test/java";

	public LoadFromRelativeFile(Class<?> storyClass) {
		this(storyClass, TEST_DIR);
	}

	public LoadFromRelativeFile(Class<?> storyClass, String traversal) {
		this.traversal = traversal;
		this.location = locationFor(storyClass);
	}

	protected URL locationFor(Class<?> storyClass) {
		return storyClass.getProtectionDomain().getCodeSource().getLocation();
	}

	public String loadStoryAsText(String storyPath) {
		String fileLocation = null;
		try {
			fileLocation = new File(location.getFile()).getCanonicalPath()
					+ "/";
			fileLocation = fileLocation + traversal + "/" + storyPath;
			fileLocation = fileLocation.replace("/", File.separator); 
			File file = new File(fileLocation);
			return IOUtils.toString(new FileInputStream(file));
		} catch (IOException e) {
			throw new InvalidStoryResourceException("Story path '" + storyPath
					+ "' not found while looking in '" + fileLocation + "'", e);
		}

	}

}