package org.jbehave.core.io.odf;

import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.odf.OdfUtils.OdfDocumentLoadingFailed;
import org.jbehave.core.io.odf.OdfUtils.OdfDocumentParsingFailed;
import org.junit.Assert;
import org.junit.Test;

public class OdtLoaderBehaviour {

    @Test
    public void shouldLoadOdtResourceFromClasspath(){
        String resourceFromOdt = new LoadOdtFromClasspath(this.getClass()).loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.odt");
        String resourceFromTxt = new LoadFromClasspath(this.getClass()).loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.txt");
        Assert.assertEquals(resourceFromOdt, resourceFromTxt);
    }
    
    @Test(expected=InvalidStoryResource.class)
    public void shouldNotLoadOdtResourceFromInexistingClasspathResource(){
        new LoadOdtFromClasspath(this.getClass()).loadResourceAsText("org/jbehave/core/io/odf/stories/an_inexisting_story.odt");
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
        new LoadOdtFromURL().loadResourceAsText(location+"org/jbehave/core/io/odf/stories/an_inexisting_story.odt");
    }
    
    @Test(expected=OdfDocumentLoadingFailed.class)
    public void shouldNotLoadInvalidOdfResources(){
        OdfUtils.loadOdt(null);
    }

    @Test(expected=OdfDocumentParsingFailed.class)
    public void shouldNotParseInvalidOdfResources(){
        OdfUtils.parseOdt(null);
    }

    @Test
    public void shouldKeepCoberturaHappy(){
        Assert.assertNotNull(new OdfUtils());
    }

}
