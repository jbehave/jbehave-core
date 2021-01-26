package org.jbehave.core.io.odf;

import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.odf.OdfUtils.OdfDocumentLoadingFailed;
import org.jbehave.core.io.odf.OdfUtils.OdfDocumentParsingFailed;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OdtLoaderBehaviour {

    @Test
    public void shouldLoadOdtResourceFromClasspath() {
        String resourceFromOdtWithTable = new LoadOdtFromClasspath(this.getClass())
                .loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.odt");
        String resourceFromTxt = new LoadFromClasspath(this.getClass())
                .loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.txt");
        assertThatOutputIs(resourceFromOdtWithTable.trim(), resourceFromTxt.trim());
    }

    @Test
    public void shouldNotLoadOdtResourceFromInexistingClasspathResource() {
        LoadOdtFromClasspath odtLoader = new LoadOdtFromClasspath(this.getClass());
        assertThrows(InvalidStoryResource.class,
                () -> odtLoader.loadResourceAsText("org/jbehave/core/io/odf/stories/an_inexisting_story.odt"));
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

    @Test
    public void shouldNotLoadOdtResourceFromInvalidURL() {
        // not a URL
        String location = CodeLocations.codeLocationFromClass(this.getClass()).getFile();
        LoadOdtFromURL odtLoader = new LoadOdtFromURL();
        assertThrows(InvalidStoryResource.class,
                () -> odtLoader.loadResourceAsText(location + "org/jbehave/core/io/odf/stories/a_story.odt"));
    }

    @Test
    public void shouldNotLoadOdtResourceFromInexistingURL() {
        String location = CodeLocations.codeLocationFromClass(this.getClass()).toString();
        LoadOdtFromURL odtLoader = new LoadOdtFromURL();
        assertThrows(InvalidStoryResource.class, () -> odtLoader
                .loadResourceAsText(location + "org/jbehave/core/io/odf/stories/an_inexisting_story.odt"));
    }

    @Test
    public void shouldNotLoadInvalidOdfResources() {
        assertThrows(OdfDocumentLoadingFailed.class, () -> OdfUtils.loadOdt(null));
    }

    @Test
    public void shouldNotParseInvalidOdfResources() {
        assertThrows(OdfDocumentParsingFailed.class, () -> OdfUtils.parseOdt(null));
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
