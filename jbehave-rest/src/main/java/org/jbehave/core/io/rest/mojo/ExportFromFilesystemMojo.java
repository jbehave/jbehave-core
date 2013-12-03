package org.jbehave.core.io.rest.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.ResourceExporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.ResourceUploader;
import org.jbehave.core.io.rest.filesystem.ExportFromFilesystem;
import org.jbehave.core.io.rest.redmine.IndexFromRedmine;
import org.jbehave.core.io.rest.redmine.UploadToRedmine;

/**
 * Mojo to export resources to REST root path from filesystem source path.
 * 
 * @goal export-from-filesystem
 * @requiresProject false
 */
public class ExportFromFilesystemMojo extends AbstractMojo {

    private static final String REDMINE = "redmine";

    /**
     * The REST provider
     * 
     * @parameter default-value="redmine" expression="${jbehave.restProvider}
     */
    String restProvider;

    /**
     * The root URI of the REST API
     * 
     * @parameter expression="${jbehave.restRootURI}
     * @required
     */
    String restRootURI;

    /**
     * The username to access the REST API. May be null if no security enabled.
     * 
     * @parameter expression="${jbehave.restUsername}
     */
    String restUsername;

    /**
     * The password to access the REST API. May be null if no security enabled.
     * 
     * @parameter expression="${jbehave.restPassword}
     */
    String restPassword;

    /**
     * The source path of the filesystem from which the resources are read
     * 
     * @parameter default-value="target/stories"
     *            expression="${jbehave.sourcePath}
     */
    String sourcePath;

    /**
     * The extension of the files read
     * 
     * @parameter default-value=".story" expression="${jbehave.sourceExt}
     */
    String sourceExt;

    /**
     * The path to the resource index
     * 
     * @parameter expression="${jbehave.indexPath}
     */
    String indexPath;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Exporting from filesystem resources to REST root URI " + restRootURI);
            ResourceExporter exporter = createExporter();
            exporter.exportResources(restRootURI);
        } catch (Exception e) {
            String message = "Failed to export from filesystem resources to REST root URI " + restRootURI;
            getLog().warn(message);
            throw new MojoExecutionException(message, e);
        }
    }

    private ResourceExporter createExporter() {
        ResourceIndexer indexer = newResourceIndexer();
        ResourceUploader uploader = newResourceUploader();
        getLog().info(
                "Creating exporter from filesystem with with indexer " + indexer.getClass() + ", uploader "
                        + uploader.getClass() + ", sourcePath " + sourcePath + ", sourceExt " + sourceExt
                        + " and indexPath " + indexPath);
        return new ExportFromFilesystem(indexer, uploader, sourcePath, sourceExt, indexPath);
    }

    ResourceIndexer newResourceIndexer() {
        if (restProvider.equals(REDMINE)) {
            return new IndexFromRedmine(restUsername, restPassword);
        }
        throw new RuntimeException("Unsupported REST provider " + restProvider);
    }

    ResourceUploader newResourceUploader() {
        if (restProvider.equals(REDMINE)) {
            return new UploadToRedmine(Type.JSON, restUsername, restPassword);
        }
        throw new RuntimeException("Unsupported REST provider " + restProvider);
    }

}
