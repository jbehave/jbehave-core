package org.jbehave.core.io.rest.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.ResourceImporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.filesystem.ImportToFilesystem;

/**
 * Mojo to import resources from REST root path to filesystem.
 * 
 * @goal import-to-filesystem
 * @requiresProject false
 */
public class ImportToFilesystemMojo extends AbstractFilesystemMojo {

	@Override
    public void execute() throws MojoExecutionException {
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
						+ restProvider + " with resourcesPath " + resourcesPath
						+ " and resourcesExt " + resourcesExt);
		return new ImportToFilesystem(indexer, loader, resourcesPath, resourcesExt);
	}

}
