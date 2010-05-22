package org.jbehave.core.parsers;

import org.jbehave.core.model.Story;

/**
 * <p>
 * Parses the stories contained in a story from a textual representation.
 * </p>
 */
public interface StoryParser {

    /**
     * Parses story from its textual representation
     * 
     * @param storyAsText the textual representation
     * @return The Story
     */
    Story parseStory(String storyAsText);
    
    /**
     * Parses story from its textual representation and (optional) story path
     * 
     * @param storyAsText the textual representation
     * @param storyPath the story path, may be <code>null</code>
     * @return The Story
     * @since 2.4
     */
    Story parseStory(String storyAsText, String storyPath);

}
