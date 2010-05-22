package org.jbehave.core.io;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jbehave.core.io.StoryLocation;
import org.junit.Test;

public class StoryLocationBehaviour {

    @Test
    public void canHandleURLs() {
        String codeLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        String storyPath = "file:" + codeLocation + "org/jbehave/core/io/stories/my_pending_story";
        StoryLocation storyLocation = new StoryLocation(storyPath, this.getClass());
        assertThat(storyLocation.getPath(), equalTo(storyPath));
        assertThat(storyLocation.getLocation(), equalTo(storyPath));
        assertThat(storyLocation.getName(), equalTo("org/jbehave/core/io/stories/my_pending_story"));
    }
                                 
    @Test
    public void canHandleClasspathResources() {
        String storyPath = "org/jbehave/core/io/stories/my_pending_story";
        StoryLocation storyLocation = new StoryLocation(storyPath, this.getClass());
        assertThat(storyLocation.getPath(), equalTo(storyPath));
        assertThat(storyLocation.getLocation(), equalTo(storyLocation.getCodeLocation() + storyPath));
        assertThat(storyLocation.getName(), equalTo(storyPath));
    }

}