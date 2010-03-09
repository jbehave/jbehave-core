package org.jbehave.scenario.parser;

import org.jbehave.scenario.definition.StoryDefinition;

/**
 * <p>
 * Parses the scenarios contained in a story from a textual representation.
 * </p>
 */
public interface ScenarioParser {

    /**
     * Defines story from its textual representation
     * 
     * @param storyAsText the textual representation
     * @return The StoryDefinition
     */
    StoryDefinition defineStoryFrom(String storyAsText);
    
    /**
     * Defines story from its textual representation and (optional) story path
     * 
     * @param storyAsText the textual representation
     * @param storyPath the story path, may be <code>null</code>
     * @return The StoryDefinition
     * @since 2.4
     */
    StoryDefinition defineStoryFrom(String storyAsText, String storyPath);

}
