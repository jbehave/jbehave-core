package org.jbehave.core.io;

import java.util.List;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

public class JarFileScannerBehaviour {

    @Test
    public void shouldScanJarFromPath() {
        List<String> paths = scan("src/test/resources/stories.jar", "**/*.story", "**/*_search.story");
        assertThat(paths.size(), equalTo(2));
        assertThat(paths, hasItems("etsy_browse.story", "etsy_cart.story"));
        assertThat(paths, not(hasItems("etsy_search.story")));
    }

    @Test
    public void shouldScanJarFromPathWithNoExcludes() {
        List<String> emptyExcludes = scan("src/test/resources/stories.jar", "**/*.story", "");
        assertThat(emptyExcludes, hasItems("etsy_browse.story", "etsy_cart.story", "etsy_search.story"));
        assertThat(emptyExcludes, not(hasItems("etsy_steps.xml")));
        List<String> nullExcludes = scan("src/test/resources/stories.jar", "**/*.story", null);
        assertThat(nullExcludes, hasItems("etsy_browse.story", "etsy_cart.story", "etsy_search.story"));
        assertThat(nullExcludes, not(hasItems("etsy_steps.xml")));
    }

    @Test
    public void shouldScanJarFromPathWithNoIncludes() {
        List<String> emptyIncludes = scan("src/test/resources/stories.jar", "", "**/*.story");
        assertThat(emptyIncludes, not(hasItems("etsy_browse.story", "etsy_cart.story", "etsy_search.story", "etsy_steps.xml")));
        List<String> nullIncludes = scan("src/test/resources/stories.jar", null, "**/*.story");
        assertThat(nullIncludes, not(hasItems("etsy_browse.story", "etsy_cart.story", "etsy_search.story", "etsy_steps.xml")));
    }

    @Test
    public void shouldScanJarFromPathWithNullIncludesNorExcludes() {
        List<String> nullIncludesAndExcludes = scan("src/test/resources/stories.jar", (List<String>) null, null);
        assertThat(nullIncludesAndExcludes,
                hasItems("etsy_browse.story", "etsy_cart.story", "etsy_search.story", "etsy-steps.xml"));
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowIllegalArgument() {
        scan("nonexistent.jar", "", "");
    }

    private List<String> scan(String jarPath, String includes, String excludes) {
        return new JarFileScanner(jarPath, includes, excludes).scan();
    }

    private List<String> scan(String jarPath, List<String> includes, List<String> excludes) {
        return new JarFileScanner(jarPath, includes, excludes).scan();
    }

}
