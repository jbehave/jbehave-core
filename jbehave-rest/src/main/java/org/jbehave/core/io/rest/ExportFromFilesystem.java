package org.jbehave.core.io.rest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * Implementation that reads from filesystem the resources and uploads them.
 * 
 * A local updated copy of the index may be provided, via a filesystem path. If
 * the index path is <code>null</code>, the index will be retrieved by the
 * {@link ResourceIndexer} directly from the root URI.
 * 
 * The exporter requires an instance of a {@link ResourceIndexer} and of a
 * {@link ResourceUploader}.
 */
public class ExportFromFilesystem implements ResourceExporter {

    private final ResourceIndexer indexer;
    private final ResourceUploader uploader;
    private final String sourcePath;
    private final String sourceExt;
    private String indexPath;

    public ExportFromFilesystem(ResourceIndexer indexer, ResourceUploader uploader, String sourcePath, String sourceExt) {
        this(indexer, uploader, sourcePath, sourceExt, null);
    }

    public ExportFromFilesystem(ResourceIndexer indexer, ResourceUploader uploader, String sourcePath,
            String sourceExt, String indexPath) {
        this.indexer = indexer;
        this.uploader = uploader;
        this.sourcePath = sourcePath;
        this.sourceExt = sourceExt;
        this.indexPath = indexPath;
    }

    public void exportResources(String rootURI) {
        Map<String, Resource> index = (indexPath == null ? indexer.indexResources(rootURI) : indexer.indexResources(
                rootURI, indexEntity(indexPath)));
        readResources(index, sourcePath, sourceExt);
        uploadResources(index);
    }

    private String indexEntity(String indexPath) {
        try {
            return FileUtils.readFileToString(new File(indexPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read index from " + indexPath, e);
        }
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
