package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

class LoadFromRelativeFileBehaviour {

    @Test
    void testLoadFromRelativeFile() throws MalformedURLException {
        URL baseLocation = CodeLocations.codeLocationFromClass(LoadFromRelativeFileBehaviour.class);
        URL subdir = new URL(baseLocation + "/test+dir");
        LoadFromRelativeFile load = new LoadFromRelativeFile(subdir);
        String storyText = load.loadStoryAsText("dummy.story");
        assertThat(storyText, equalTo("dummy story file"));
    }

}
