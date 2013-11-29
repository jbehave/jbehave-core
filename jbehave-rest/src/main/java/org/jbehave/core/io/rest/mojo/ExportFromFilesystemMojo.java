package org.jbehave.core.io.rest.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.io.rest.ExportFromFilesystem;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.ResourceExporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.ResourceUploader;
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
     * The root path of the REST API
     * 
     * @parameter expression="${jbehave.restRootPath}
     * @required
     */
    String restRootPath;

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

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Exporting from filesystem resources to REST root path " + restRootPath);
            ResourceExporter exporter = createExporter();
            exporter.exportResources(restRootPath);
        } catch (Exception e) {
            String message = "Failed to export from filesystem resources to REST root path " + restRootPath;
            getLog().warn(message);
            throw new MojoExecutionException(message, e);
        }
    }

    private ResourceExporter createExporter() {
        ResourceIndexer indexer = newResourceIndexer();
        ResourceUploader uploader = newResourceUploader();
        return new ExportFromFilesystem(indexer, uploader, sourcePath, sourceExt);
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
