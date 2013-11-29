package org.jbehave.core.io.rest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * Implementation that reads from filesystem the resources and uploads them.
 * 
 * The exporter requires an instance of a {@link ResourceIndexer} and of a
 * {@link ResourceUploader}.
 */
public class ExportFromFilesystem implements ResourceExporter {

    private final ResourceIndexer indexer;
    private final ResourceUploader uploader;
    private final String sourcePath;
    private final String sourceExt;

    public ExportFromFilesystem(ResourceIndexer indexer, ResourceUploader uploader, String sourcePath, String sourceExt) {
        this.indexer = indexer;
        this.uploader = uploader;
        this.sourcePath = sourcePath;
        this.sourceExt = sourceExt;
    }

    public void exportResources(String rootURI) {
        Map<String, Resource> index = indexer.indexResources(rootURI);
        readResources(index, sourcePath, sourceExt);
        uploadResources(index);
    }

    private void uploadResources(Map<String, Resource> index) {
        for (String name : index.keySet()) {
            Resource resource = index.get(name);
            uploader.uploadResourceAsText(resource.getURI(), resource.getText());
        }
    }

    private void readResources(Map<String, Resource> index, String sourcePath, String sourceExt) {
        for (String name : index.keySet()) {
            Resource resource = index.get(name);
            File file = new File(sourcePath, name + sourceExt);
            readResource(resource, file);
        }
    }

    private void readResource(Resource resource, File file) {
        try {
            String text = FileUtils.readFileToString(file);
            resource.setText(text);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource " + resource + " from file " + file, e);
        }
    }

}
