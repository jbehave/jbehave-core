package org.jbehave.core.io.rest.filesystem;

import static org.jbehave.core.io.rest.filesystem.FilesystemUtils.asFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceExporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.ResourceUploader;

/**
 * Implementation that reads from filesystem the resources and uploads them.
 * 
 * An include pattern of the resources may be provided.
 * 
 * The exporter requires an instance of a {@link ResourceIndexer} and of a
 * {@link ResourceUploader}.
 */
public class ExportFromFilesystem implements ResourceExporter {

    private final ResourceIndexer indexer;
    private final ResourceUploader uploader;
    private final String sourcePath;
    private final String sourceExt;
    private String syntax;
    private String includes;

    public ExportFromFilesystem(ResourceIndexer indexer,
            ResourceUploader uploader, String sourcePath, String sourceExt,
            String syntax, String includes) {
        this.indexer = indexer;
        this.uploader = uploader;
        this.sourcePath = sourcePath;
        this.sourceExt = sourceExt;
        this.syntax = syntax;
        this.includes = includes;
    }

    @Override
    public void exportResources(String rootURI) {
        Map<String, Resource> index = indexer.indexResources(rootURI,
                sourcePath, syntax, includes);
        readResources(index, sourcePath, sourceExt);
        uploadResources(index);
    }

    private void uploadResources(Map<String, Resource> index) {
        for (String name : index.keySet()) {
            Resource resource = index.get(name);
            uploader.uploadResource(resource);
        }
    }

    private void readResources(Map<String, Resource> index, String sourcePath,
            String sourceExt) {
        for (String name : index.keySet()) {
            Resource resource = index.get(name);
            readResource(resource, asFile(resource, sourcePath, sourceExt));
        }
    }

    private void readResource(Resource resource, File file) {
        if ( file.isDirectory() || !file.exists() ) return;
        try {
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            resource.setContent(text);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource " + resource
                    + " from file " + file, e);
        }
    }

}
