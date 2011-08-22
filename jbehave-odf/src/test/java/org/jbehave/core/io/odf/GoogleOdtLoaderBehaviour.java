package org.jbehave.core.io.odf;

import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.odf.LoadOdtFromGoogle.GoogleAccessFailed;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class GoogleOdtLoaderBehaviour {

    @Test
    @Ignore("Need to set up test account in Google")
    public void shouldLoadFromGoogleDocs() {
        String resourceFromGoogleDocs = googleStoryLoader().loadStoryAsText("a_story");
        String resourceFromTxt = new LoadFromClasspath(this.getClass())
                .loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.txt");
        Assert.assertEquals(resourceFromTxt.trim(), resourceFromGoogleDocs.trim());
    }

    @Test(expected = InvalidStoryResource.class)
    @Ignore("Need to set up test account in Google")
    public void shouldNotLoadInexistingResourceFromGoogleDocs() {
        googleStoryLoader().loadStoryAsText("an_inexisting_story");
    }

    @Test(expected = GoogleAccessFailed.class)
    public void shouldNotAllowInvalidAccess() {
        new LoadOdtFromGoogle("DUMMY", "DUMMY");
    }

    private StoryLoader googleStoryLoader() {
        String user = System.getenv("GOOGLE_USER");
        String password = System.getenv("GOOGLE_PASSWORD");
        return new LoadOdtFromGoogle(user, password);
    }

}
