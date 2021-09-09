package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *  Groups a set of {@link Story}s by meta filter. 
 */
public class StoryMap {

    private final String metaFilter;
    private final Set<Story> stories;
    
    public StoryMap(String metaFilter, Set<Story> stories) {
        this.metaFilter = metaFilter;
        this.stories = stories;        
    }

    public String getMetaFilter() {
        return metaFilter;
    }
    
    public List<Story> getStories() {
        return new ArrayList<>(stories);
    }
    
    public List<String> getStoryPaths() {
        List<String> paths = new ArrayList<>();
        for (Story story : stories) {
            paths.add(story.getPath());
        }
        return paths;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(metaFilter).append(getStoryPaths())
                .toString();
    }

}
