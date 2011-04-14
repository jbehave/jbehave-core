package org.jbehave.core.io.odf;

import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.LoadFromURL;
import org.junit.Assert;
import org.junit.Test;

public class OdtLoaderBehaviour {

    @Test
    public void shouldLoadOdtResourceFromClasspath(){
        String resourceFromOdt = new LoadOdtFromClasspath(this.getClass()).loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.odt");
        String resourceFromTxt = new LoadFromClasspath(this.getClass()).loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.txt");
        Assert.assertEquals(resourceFromOdt, resourceFromTxt);
    }
    
    @Test
    public void shouldLoadOdtResourceFromURL(){
        String location = CodeLocations.codeLocationFromClass(this.getClass()).toString();        
        String resourceFromOdt = new LoadOdtFromURL().loadResourceAsText(location+"org/jbehave/core/io/odf/stories/a_story.odt");
        String resourceFromTxt = new LoadFromURL().loadResourceAsText(location+"org/jbehave/core/io/odf/stories/a_story.txt");
        Assert.assertEquals(resourceFromOdt, resourceFromTxt);
    }

    @Test(expected=InvalidStoryResource.class)
    public void shouldNotLoadOdtResourceFromInvalidURL(){
        String location = CodeLocations.codeLocationFromClass(this.getClass()).getFile(); // not a URL        
        new LoadOdtFromURL().loadResourceAsText(location+"org/jbehave/core/io/odf/stories/a_story.odt");
    }

    @Test(expected=InvalidStoryResource.class)
    public void shouldNotLoadOdtResourceFromInexistingURL(){
        String location = CodeLocations.codeLocationFromClass(this.getClass()).toString();        
        new LoadOdtFromURL().loadResourceAsText(location+"org/jbehave/core/io/odf/stories/a_inexisting_story.odt");
    }
}
