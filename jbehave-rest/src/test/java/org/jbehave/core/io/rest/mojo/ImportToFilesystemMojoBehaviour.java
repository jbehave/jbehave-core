package org.jbehave.core.io.rest.mojo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.junit.Test;

import static org.apache.commons.io.FileUtils.readFileToString;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImportToFilesystemMojoBehaviour {

    @Test
    public void canImportToFilesystem() throws IOException, MojoExecutionException, MojoFailureException {

        // Given
        final ResourceIndexer indexer = mock(ResourceIndexer.class);
        final ResourceLoader loader = mock(ResourceLoader.class);
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
        ImportToFilesystemMojo mojo = new ImportToFilesystemMojo(){

            @Override
            ResourceIndexer newResourceIndexer() {
                return indexer;
            }

            @Override
            ResourceLoader newResourceLoader() {
                return loader;
            }
            
        };
        mojo.restProvider = "wiki";
        mojo.restRootURI = rootURI;
        mojo.resourcesPath = targetPath;
        mojo.resourcesExt = targetExt;
        
        mojo.execute();
        
        // Then
        File file1 = new File(targetPath + "/one" + targetExt);
        assertThat(file1.exists(), equalTo(true));
        assertThat(readFileToString(file1), equalTo(text1));
        File file2 = new File(targetPath + "/two" + targetExt);
        assertThat(file2.exists(), equalTo(true));
        assertThat(readFileToString(file2), equalTo(text2));

    }

}
