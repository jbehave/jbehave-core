package org.jbehave.core.io;

import static org.apache.commons.lang3.StringUtils.removeStart;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * Abstraction of a story location, handling cases in which story path is defined
 * as a resource in classpath or as a URL.
 * </p>
 * <p>Given a code location URL and a story path, it provides the methods:
 * <ul>
 * <li>{@link #getURL()}: the story location URL, prefixing the code location external form if story path is not a
 * URL</li>
 * <li>{@link #getPath()}: the story location path, removing the code location external form if story path is a URL</li>
 * </ul>
 * </p>
 */
public class StoryLocation {

    private final URL codeLocation;
    private final String storyPath;
    private final boolean storyPathIsURL;

    public StoryLocation(URL codeLocation, String storyPath) {
        this.codeLocation = codeLocation;
        this.storyPath = storyPath;
        this.storyPathIsURL = isURL(storyPath);
    }

    public URL getCodeLocation() {
        return codeLocation;
    }

    public String getStoryPath() {
        return storyPath;
    }

    public String getURL() {
        if (storyPathIsURL) {
            return storyPath;
        } else {
            return codeLocation.toExternalForm() + storyPath;
        }
    }

    public String getPath() {
        if (storyPathIsURL) {
            return removeStart(storyPath, codeLocation.toExternalForm());
        } else {
            return storyPath;
        }
    }

    private boolean isURL(String path) {
        try {
            new URL(path);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, 
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}