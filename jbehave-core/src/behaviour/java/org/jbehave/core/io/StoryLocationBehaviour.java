package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class StoryLocationBehaviour {

    @Test
    public void shouldHandleClasspathResources() {
        URL codeLocation = StoryLocation.codeLocationFromClass(this.getClass());
        String storyName = "org/jbehave/core/io/stories/my_pending_story";
        String storyPath = storyName;
		StoryLocation storyLocation = new StoryLocation(codeLocation, storyPath);
        assertThat(storyLocation.getPath(), equalTo(storyPath));
        assertThat(storyLocation.isURL(), is(false));
        assertThat(storyLocation.toString(), containsString("url=false"));
        assertThat(storyLocation.getLocation(), equalTo(codeLocation + storyName));
        assertThat(storyLocation.getName(), equalTo(storyName));
    }

    @Test
    public void shouldHandleURLResourcesSpecifiedViaClassCodeLocation() {
        URL codeLocation = StoryLocation.codeLocationFromClass(this.getClass());
        String storyName = "org/jbehave/core/io/stories/my_pending_story";
		String storyPath = codeLocation + storyName;
		StoryLocation storyLocation = new StoryLocation(codeLocation, storyPath);
        assertThat(storyLocation.getPath(), equalTo(storyPath));
        assertThat(storyLocation.isURL(), is(true));
        assertThat(storyLocation.toString(), containsString("url=true"));
        assertThat(storyLocation.getLocation(), equalTo(storyPath));
        assertThat(storyLocation.getName(), equalTo(storyName));
    }

    @Test
    public void shouldHandleURLResourcesSpecifiedViaFileCodeLocation() {
        URL codeLocation = StoryLocation.codeLocationFromFile(new File("target/test-classes"));
        String storyName = "org/jbehave/core/io/stories/my_pending_story";
		String storyPath = codeLocation + storyName;
		StoryLocation storyLocation = new StoryLocation(codeLocation, storyPath);
        assertThat(storyLocation.getPath(), equalTo(storyPath));
        assertThat(storyLocation.isURL(), is(true));
        assertThat(storyLocation.toString(), containsString("url=true"));
        assertThat(storyLocation.getLocation(), equalTo(storyPath));
        assertThat(storyLocation.getName(), equalTo(storyName));
    }

}