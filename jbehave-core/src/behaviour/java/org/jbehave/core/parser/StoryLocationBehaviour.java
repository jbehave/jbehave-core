package org.jbehave.core.parser;

import static org.hamcrest.Matchers.equalTo;
import static org.jbehave.Ensure.ensureThat;

import org.junit.Test;

public class StoryLocationBehaviour {

    @Test
    public void canHandleURLs() {
        String codeLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        String storyPath = "file:" + codeLocation + "org/jbehave/core/parser/stories/my_pending_story";
        StoryLocation storyLocation = new StoryLocation(storyPath, this.getClass());
        ensureThat(storyLocation.getPath(), equalTo(storyPath));
        ensureThat(storyLocation.getLocation(), equalTo(storyPath));
        ensureThat(storyLocation.getName(), equalTo("org/jbehave/core/parser/stories/my_pending_story"));
    }
                                 
    @Test
    public void canHandleClasspathResources() {
        String storyPath = "org/jbehave/core/parser/stories/my_pending_story";
        StoryLocation storyLocation = new StoryLocation(storyPath, this.getClass());
        ensureThat(storyLocation.getPath(), equalTo(storyPath));
        ensureThat(storyLocation.getLocation(), equalTo(storyLocation.getCodeLocation() + storyPath));
        ensureThat(storyLocation.getName(), equalTo(storyPath));
    }

}