package org.jbehave.core.io;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

public class JarFileScannerBehaviour {

    @Test
    public void shouldScanFileFromPath() throws IOException {
        String jarPath = "src/test/resources/stories.jar";
        JarFileScanner scanner = new JarFileScanner(jarPath, "**/*.story", "**/*_search.story");
        assertThat(scanner.scan(), equalTo(asList("etsy_browse.story", "etsy_cart.story")));
    }

    @Test
    public void shouldScanFileFromURL() throws IOException {
        URL jarURL = CodeLocations.codeLocationFromPath("src/test/resources/stories.jar");
        JarFileScanner scanner = new JarFileScanner(jarURL, "**/*.story", "**/*_search.story");
        assertThat(scanner.scan(), equalTo(asList("etsy_browse.story", "etsy_cart.story")));
    }

}
