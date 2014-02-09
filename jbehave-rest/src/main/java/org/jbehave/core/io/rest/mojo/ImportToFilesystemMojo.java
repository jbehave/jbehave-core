package org.jbehave.core.io.rest.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.ResourceImporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.filesystem.ImportToFilesystem;

/**
 * Mojo to import resources from REST root path to filesystem target path.
 * 
 * @goal import-to-filesystem
 * @requiresProject false
 */
public class ImportToFilesystemMojo extends AbstractFilesystemMojo {

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
			getLog().info(
					"Importing to filesystem resources from REST root URI "
							+ restRootURI);
			ResourceImporter importer = createImporter();
			importer.importResources(restRootURI);
		} catch (Exception e) {
			String message = "Failed to import to filesystem resources from REST root URI "
					+ restRootURI;
			getLog().warn(message);
			throw new MojoExecutionException(message, e);
		}
	}

	private ResourceImporter createImporter() {
		ResourceIndexer indexer = newResourceIndexer();
		ResourceLoader loader = newResourceLoader();
		getLog().info(
				"Creating importer to filesystem using REST provider "
						+ restProvider + " with targetPath " + targetPath
						+ ", targetExt " + targetExt);
		return new ImportToFilesystem(indexer, loader, targetPath, targetExt);
	}

}
