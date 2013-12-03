package org.jbehave.core.io.rest.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.ImportToFilesystem;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.ResourceImporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.redmine.IndexFromRedmine;
import org.jbehave.core.io.rest.redmine.LoadFromRedmine;

/**
 * Mojo to import resources from REST root path to filesystem target path.
 * 
 * @goal import-to-filesystem
 * @requiresProject false
 */
public class ImportToFilesystemMojo extends AbstractMojo {

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
     * The target path of the filesystem to which the resources are written
     * 
     * @parameter default-value="target/stories"
     *            expression="${jbehave.targetPath}
     */
    String targetPath;

    /**
     * The extension of the files written
     * 
     * @parameter default-value=".story" expression="${jbehave.targetExt}
     */
    String targetExt;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Importing to filesystem resources from REST root URI " + restRootURI);
            ResourceImporter importer = createImporter();
            importer.importResources(restRootURI);
        } catch (Exception e) {
            String message = "Failed to import to filesystem resources from REST root URI " + restRootURI;
            getLog().warn(message);
            throw new MojoExecutionException(message, e);
        }
    }

    private ResourceImporter createImporter() {
        ResourceIndexer indexer = newResourceIndexer();
        ResourceLoader loader = newResourceLoader();
        getLog().info(
                "Creating importer to filesystem with indexer " + indexer.getClass() + ", loader " + loader.getClass()
                        + ", targetPath " + targetPath + ", targetExt " + targetExt);
        return new ImportToFilesystem(indexer, loader, targetPath, targetExt);
    }

    ResourceIndexer newResourceIndexer() {
        if (restProvider.equals(REDMINE)) {
            return new IndexFromRedmine(restUsername, restPassword);
        }
        throw new RuntimeException("Unsupported REST provider " + restProvider);
    }

    ResourceLoader newResourceLoader() {
        if (restProvider.equals(REDMINE)) {
            return new LoadFromRedmine(Type.JSON, restUsername, restPassword);
        }
        throw new RuntimeException("Unsupported REST provider " + restProvider);
    }

}
