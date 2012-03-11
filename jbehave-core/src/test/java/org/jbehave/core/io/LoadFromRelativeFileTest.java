package org.jbehave.core.io;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class LoadFromRelativeFileTest {

    @Test
    public void testLoadFromRelativeFile() throws MalformedURLException {
        URL baseLocation = CodeLocations.codeLocationFromClass(LoadFromRelativeFileTest.class);
        URL subdir=new URL(baseLocation.toString()+"/test+dir");
        LoadFromRelativeFile load = new LoadFromRelativeFile(subdir);
        String storyText=load.loadStoryAsText("dummy.story");
        assertThat(storyText, equalTo("dummy story file"));
    }

}
