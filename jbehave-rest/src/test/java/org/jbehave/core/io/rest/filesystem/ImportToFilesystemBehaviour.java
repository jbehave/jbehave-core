package org.jbehave.core.io.rest.filesystem;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceImporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.junit.jupiter.api.Test;

class ImportToFilesystemBehaviour {

    @Test
    void canImportToFilesystem() throws IOException {

        // Given
        ResourceIndexer indexer = mock(ResourceIndexer.class);
        ResourceLoader loader = mock(ResourceLoader.class);
        String rootURI = "http://wiki";
        Map<String, Resource> index = new HashMap<>();
        index.put("one", new Resource(rootURI + "/one"));
        index.put("two", new Resource(rootURI + "/two"));
        when(indexer.indexResources(rootURI)).thenReturn(index);
        String text1 = "story text 1";
        when(loader.loadResourceAsText(index.get("one").getURI())).thenReturn(text1);
        String text2 = "story text 2";
        when(loader.loadResourceAsText(index.get("two").getURI())).thenReturn(text2);

        // When
        String targetPath = "target/stories";
        String targetExt = ".story";
        ResourceImporter importer = new ImportToFilesystem(indexer, loader, targetPath, targetExt);
        importer.importResources(rootURI);

        // Then
        File file1 = new File(targetPath + "/one" + targetExt);
        assertThat(file1.exists(), equalTo(true));
        assertThat(readFileToString(file1, StandardCharsets.UTF_8), equalTo(text1));
        File file2 = new File(targetPath + "/two" + targetExt);
        assertThat(file2.exists(), equalTo(true));
        assertThat(readFileToString(file2, StandardCharsets.UTF_8), equalTo(text2));

    }

}
