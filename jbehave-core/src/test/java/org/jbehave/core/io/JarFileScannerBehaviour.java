package org.jbehave.core.io;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

public class JarFileScannerBehaviour {

    @Test
    public void shouldScanJarFromPath() throws IOException {
        List<String> paths = scan("src/test/resources/stories.jar", "**/*.story", "**/*_search.story");
        assertThat(paths, hasItems("etsy_browse.story", "etsy_cart.story"));
        assertThat(paths, not(hasItems("etsy_search.story")));
    }

    @Test
    public void shouldScanJarFromPathWithNoExcludes() throws IOException {
        assertThat(scan("src/test/resources/stories.jar", "**/*.story", ""),
                hasItems("etsy_browse.story", "etsy_cart.story", "etsy_search.story"));
        assertThat(scan("src/test/resources/stories.jar", "**/*.story", null),
                hasItems("etsy_browse.story", "etsy_cart.story", "etsy_search.story"));
    }

    @Test
    public void shouldScanJarFromPathWithNoIncludes() throws IOException {
        assertThat(scan("src/test/resources/stories.jar", "", "**/*.story"),
                not(hasItems("etsy_browse.story", "etsy_cart.story", "etsy_search.story")));
        assertThat(scan("src/test/resources/stories.jar", null, "**/*.story"),
                not(hasItems("etsy_browse.story", "etsy_cart.story", "etsy_search.story")));
    }

    private List<String> scan(String jarPath, String includes, String excludes) {
        return new JarFileScanner(jarPath, includes, excludes).scan();
    }

}
