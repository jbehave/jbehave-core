package org.jbehave.core.io;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JarFileScannerBehaviour {

    @Test
    public void shouldScanFileFromPath() throws IOException {
        String jarPath = "src/test/resources/stories.jar";
        assertEquals(asList("etsy_browse.story", "etsy_cart.story"),
                new JarFileScanner(jarPath, "**/*.story", "**/*_search.story").scan());
    }

    @Test
    public void shouldScanFileFromURL() throws IOException {
        URL jarURL = CodeLocations.codeLocationFromPath("src/test/resources/stories.jar");
        assertEquals(asList("etsy_browse.story", "etsy_cart.story"),
                new JarFileScanner(jarURL, "**/*.story", "**/*_search.story").scan());
    }

}
