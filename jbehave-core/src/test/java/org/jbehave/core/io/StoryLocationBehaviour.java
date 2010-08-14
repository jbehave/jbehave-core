package org.jbehave.core.io;

import static org.apache.commons.lang.StringUtils.removeStart;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.net.URL;

import org.junit.Test;

public class StoryLocationBehaviour {

    @Test
    public void shouldHandleClasspathResources() {
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        String storyPath = "org/jbehave/core/io/stories/my_pending_story";
		StoryLocation storyLocation = new StoryLocation(codeLocation, storyPath);
		assertThat(storyLocation.getCodeLocation(), equalTo(codeLocation));
        assertThat(storyLocation.getStoryPath(), equalTo(storyPath));
        assertThat(storyLocation.getURL(), equalTo(codeLocation + storyPath));
        assertThat(storyLocation.getPath(), equalTo(storyPath));
        assertThat(storyLocation.toString(), containsString("storyPathIsURL=false"));
    }

    @Test
    public void shouldHandleURLResources() {
        URL codeLocation = CodeLocations.codeLocationFromPath("src/test/java/");
		String storyPath = codeLocation + "org/jbehave/core/io/stories/my_pending_story";
		StoryLocation storyLocation = new StoryLocation(codeLocation, storyPath);
        assertThat(storyLocation.getCodeLocation(), equalTo(codeLocation));
        assertThat(storyLocation.getStoryPath(), equalTo(storyPath));
        assertThat(storyLocation.getURL(), equalTo(storyPath));
        assertThat(storyLocation.getPath(), equalTo(removeStart(storyPath, codeLocation.toString())));
        assertThat(storyLocation.toString(), containsString("storyPathIsURL=true"));
    }

}