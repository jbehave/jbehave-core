package org.jbehave.core.io;

import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.net.URL;

import org.junit.jupiter.api.Test;

class StoryLocationBehaviour {

    @Test
    void shouldAllowClasspathResources() {
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        String storyPath = "org/jbehave/core/io/stories/my_pending_story";
		StoryLocation storyLocation = new StoryLocation(codeLocation, storyPath);
		assertThat(storyLocation.getCodeLocation(), equalTo(codeLocation));
        assertThat(storyLocation.getStoryPath(), equalTo(storyPath));
        assertThat(storyLocation.getURL(), equalTo(codeLocation.toExternalForm() + storyPath));
        assertThat(storyLocation.getPath(), equalTo(storyPath));
        assertThat(storyLocation.toString(), containsString("storyPathIsURL=false"));
    }

    @Test
    void shouldAllowURLResources() {
        assertThatStoryLocationAllowsStoryPathAsURL(CodeLocations.codeLocationFromPath("src/test/java/"));
        assertThatStoryLocationAllowsStoryPathAsURL(CodeLocations.codeLocationFromURL("http://company.com/stories/"));
    }

    private void assertThatStoryLocationAllowsStoryPathAsURL(URL codeLocation) {
        String storyPath = codeLocation + "org/jbehave/core/io/stories/my_pending_story";
        StoryLocation storyLocation = new StoryLocation(codeLocation, storyPath);
        assertThat(storyLocation.getCodeLocation(), equalTo(codeLocation));
        assertThat(storyLocation.getStoryPath(), equalTo(storyPath));
        assertThat(storyLocation.getURL(), equalTo(storyPath));
        assertThat(storyLocation.getPath(), equalTo(removeStart(storyPath, codeLocation.toExternalForm())));
        assertThat(storyLocation.toString(), containsString("storyPathIsURL=true"));
    }


}