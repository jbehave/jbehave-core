package org.jbehave.examples.core;

import org.jbehave.core.io.StoryFinder;

import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

public class FailingCoreStories extends CoreStories {

    @Override
    protected List<String> storyPaths() {
        String filter = System.getProperty("story.filter", "**/failing/*.story");
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), filter, "**/custom/*.story,**/given/*.story,**/pending/*.story");
    }
}
