package org.jbehave.core.io.odf;

import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.odf.OdfUtils.OdfDocumentLoadingFailed;
import org.jbehave.core.io.odf.OdfUtils.OdfDocumentParsingFailed;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class OdtLoaderBehaviour {

    @Test
    public void shouldLoadOdtResourceFromClasspath() {
        String resourceFromOdtWithTable = new LoadOdtFromClasspath(this.getClass())
                .loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.odt");
        String resourceFromTxt = new LoadFromClasspath(this.getClass())
                .loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.txt");
        assertThatOutputIs(resourceFromOdtWithTable.trim(), resourceFromTxt.trim());
    }

    @Test(expected = InvalidStoryResource.class)
    public void shouldNotLoadOdtResourceFromInexistingClasspathResource() {
        new LoadOdtFromClasspath(this.getClass())
                .loadResourceAsText("org/jbehave/core/io/odf/stories/an_inexisting_story.odt");
    }

    @Test
    public void shouldLoadOdtResourceFromURL() {
        String location = CodeLocations.codeLocationFromClass(this.getClass()).toString();
        String resourceFromOdtWithTable = new LoadOdtFromURL().loadResourceAsText(location
                + "org/jbehave/core/io/odf/stories/a_story.odt");
        String resourceFromTxt = new LoadFromURL().loadResourceAsText(location
                + "org/jbehave/core/io/odf/stories/a_story.txt");
        assertThatOutputIs(resourceFromOdtWithTable.trim(), resourceFromTxt.trim());
    }

    @Test(expected = InvalidStoryResource.class)
    public void shouldNotLoadOdtResourceFromInvalidURL() {
        // not a URL
        String location = CodeLocations.codeLocationFromClass(this.getClass()).getFile();
        new LoadOdtFromURL().loadResourceAsText(location + "org/jbehave/core/io/odf/stories/a_story.odt");
    }

    @Test(expected = InvalidStoryResource.class)
    public void shouldNotLoadOdtResourceFromInexistingURL() {
        String location = CodeLocations.codeLocationFromClass(this.getClass()).toString();
        new LoadOdtFromURL().loadResourceAsText(location + "org/jbehave/core/io/odf/stories/an_inexisting_story.odt");
    }

    @Test(expected = OdfDocumentLoadingFailed.class)
    public void shouldNotLoadInvalidOdfResources() {
        OdfUtils.loadOdt(null);
    }

    @Test(expected = OdfDocumentParsingFailed.class)
    public void shouldNotParseInvalidOdfResources() {
        OdfUtils.parseOdt(null);
    }

    @Test
    public void shouldKeepCoberturaHappy() {
        assertThat(new OdfUtils(), is(notNullValue()));
    }

    // copied from core/TemplatableOutputBehaviour
    private void assertThatOutputIs(String out, String expected) {
        assertThat(dos2unix(out), equalTo(dos2unix(expected)));
    }

    private String dos2unix(String string) {
        return string.replace("\r\n", "\n");
    }

}
