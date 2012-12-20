package org.jbehave.core.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class JarFileScannerTest {

    @Test
    public void testScan() throws IOException {
        assertEquals(Arrays.asList("etsy_browse.story", "etsy_cart.story"),
                JarFileScanner.scanJar("src/test/resources/stories.jar", "**/*.story", "**/*_search.story"));
    }

}
