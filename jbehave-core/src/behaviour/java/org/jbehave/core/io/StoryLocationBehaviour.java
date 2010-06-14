package org.jbehave.core.io;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.io.StoryLocation.codeLocationFromClass;

public class StoryLocationBehaviour {

    @Test
    public void shouldHandleURLs() {
        String codeLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        String storyPath = "file:" + codeLocation + "org/jbehave/core/io/stories/my_pending_story";
        StoryLocation storyLocation = new StoryLocation(storyPath, codeLocationFromClass(this.getClass()));
        assertThat(storyLocation.isURL(), is(true));
        assertThat(storyLocation.toString(), containsString("url=true"));
        assertThat(storyLocation.getPath(), equalTo(storyPath));
        assertThat(storyLocation.getLocation(), equalTo(storyPath));
        assertThat(storyLocation.getName(), equalTo("org/jbehave/core/io/stories/my_pending_story"));
    }
                                 
    @Test
    public void shouldHandleClasspathResources() {
        String storyPath = "org/jbehave/core/io/stories/my_pending_story";
        StoryLocation storyLocation = new StoryLocation(storyPath, codeLocationFromClass(this.getClass()));
        assertThat(storyLocation.isURL(), is(false));
        assertThat(storyLocation.toString(), containsString("url=false"));
        assertThat(storyLocation.getPath(), equalTo(storyPath));
        assertThat(storyLocation.getLocation(), equalTo(storyLocation.getCodeLocation() + storyPath));
        assertThat(storyLocation.getName(), equalTo(storyPath));
    }

}