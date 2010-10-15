package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
    
    public List<Story> getStories(){
        return new ArrayList<Story>(stories);
    }
    
    public List<String> getStoryNames() {
        List<String> names = new ArrayList<String>();
        for (Story story : stories) {
            String name = StringUtils.substringAfterLast(story.getPath(), "/");
            names.add(name);            
        }
        return names;
    }

    public List<String> getStoryPaths() {
        List<String> paths = new ArrayList<String>();
        for (Story story : stories) {
            paths.add(story.getPath());
        }
        return paths;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(pattern).append(getStoryPaths()).toString();
    }

}
