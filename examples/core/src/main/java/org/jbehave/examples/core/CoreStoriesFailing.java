package org.jbehave.examples.core;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.List;

import org.jbehave.core.io.StoryFinder;

public class CoreStoriesFailing extends CoreStories {

    @Override
    public List<String> storyPaths() {
        String filter = System.getProperty("story.filter", "**/failing/*.story");
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), filter,
                "**/custom/*.story,**/given/*.story,**/pending/*.story");
    }
}
