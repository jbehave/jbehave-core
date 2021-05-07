package org.jbehave.core.io.odf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.odf.OdfUtils.OdfDocumentLoadingFailed;
import org.jbehave.core.io.odf.OdfUtils.OdfDocumentParsingFailed;
import org.junit.jupiter.api.Test;

class OdtLoaderBehaviour {

    @Test
    void shouldLoadOdtResourceFromClasspath() {
        String resourceFromOdtWithTable = new LoadOdtFromClasspath(this.getClass())
                .loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.odt");
        String resourceFromTxt = new LoadFromClasspath(this.getClass())
                .loadResourceAsText("org/jbehave/core/io/odf/stories/a_story.txt");
        assertThatOutputIs(resourceFromOdtWithTable.trim(), resourceFromTxt.trim());
    }

    @Test
    void shouldNotLoadOdtResourceFromInexistingClasspathResource() {
        LoadOdtFromClasspath odtLoader = new LoadOdtFromClasspath(this.getClass());
        assertThrows(InvalidStoryResource.class,
                () -> odtLoader.loadResourceAsText("org/jbehave/core/io/odf/stories/an_inexisting_story.odt"));
    }

    @Test
    void shouldLoadOdtResourceFromURL() {
        String location = CodeLocations.codeLocationFromClass(this.getClass()).toString();
        String resourceFromOdtWithTable = new LoadOdtFromURL().loadResourceAsText(location
                + "org/jbehave/core/io/odf/stories/a_story.odt");
        String resourceFromTxt = new LoadFromURL().loadResourceAsText(location
                + "org/jbehave/core/io/odf/stories/a_story.txt");
        assertThatOutputIs(resourceFromOdtWithTable.trim(), resourceFromTxt.trim());
    }

    @Test
    void shouldNotLoadOdtResourceFromInvalidURL() {
        // not a URL
        String location = CodeLocations.codeLocationFromClass(this.getClass()).getFile();
        LoadOdtFromURL odtLoader = new LoadOdtFromURL();
        assertThrows(InvalidStoryResource.class,
                () -> odtLoader.loadResourceAsText(location + "org/jbehave/core/io/odf/stories/a_story.odt"));
    }

    @Test
    void shouldNotLoadOdtResourceFromInexistingURL() {
        String location = CodeLocations.codeLocationFromClass(this.getClass()).toString();
        LoadOdtFromURL odtLoader = new LoadOdtFromURL();
        assertThrows(InvalidStoryResource.class, () -> odtLoader
                .loadResourceAsText(location + "org/jbehave/core/io/odf/stories/an_inexisting_story.odt"));
    }

    @Test
    void shouldNotLoadInvalidOdfResources() {
        assertThrows(OdfDocumentLoadingFailed.class, () -> OdfUtils.loadOdt(null));
    }

    @Test
    void shouldNotParseInvalidOdfResources() {
        assertThrows(OdfDocumentParsingFailed.class, () -> OdfUtils.parseOdt(null));
    }

    @Test
    void shouldKeepCoberturaHappy() {
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
