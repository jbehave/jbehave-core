package org.jbehave.core.model;

import java.util.Set;

public class StoryMap {

    private final String pattern;
    private final Set<Story> stories;
    
    public StoryMap(String pattern, Set<Story> stories) {
        this.pattern = pattern;
        this.stories = stories;        
    }

    public String getMetaPattern(){
        return pattern;
    }
    
    public Set<Story> getStories(){
        return stories;
    }
    
}
