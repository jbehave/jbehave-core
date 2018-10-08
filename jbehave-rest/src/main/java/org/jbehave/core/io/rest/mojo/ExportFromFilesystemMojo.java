package org.jbehave.core.io.rest.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.jbehave.core.io.rest.ResourceExporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.ResourceUploader;
import org.jbehave.core.io.rest.filesystem.ExportFromFilesystem;

/**
 * Mojo to export resources to REST root path from filesystem.
 * 
 * @goal export-from-filesystem
 * @requiresProject false
 */
public class ExportFromFilesystemMojo extends AbstractFilesystemMojo {

	/**
	 * The includes pattern of the resources
	 * 
	 * @parameter default-value="**"
	 *            expression="${jbehave.rest.resourcesIncludes}
	 */
	String resourcesIncludes;

	/**
	 * The syntax of the resources
	 * 
	 * @parameter default-value=""
	 * 			  expression="${jbehave.rest.resourcesSyntax}
	 */
	String resourcesSyntax;

	@Override
    public void execute() throws MojoExecutionException {
		try {
			getLog().info(
					"Exporting from filesystem resources to REST root URI "
							+ restRootURI);
			ResourceExporter exporter = createExporter();
			exporter.exportResources(restRootURI);
		} catch (Exception e) {
			String message = "Failed to export from filesystem resources to REST root URI "
					+ restRootURI;
			getLog().warn(message);
			throw new MojoExecutionException(message, e);
		}
	}

	private ResourceExporter createExporter() {
		ResourceIndexer indexer = newResourceIndexer();
		ResourceUploader uploader = newResourceUploader();
		getLog().info(
				"Creating exporter from filesystem using REST provider "
						+ restProvider + " with resourcesPath " + resourcesPath
						+ ", resourcesExt " + resourcesExt + ", resourcesSyntax "
						+ resourcesSyntax + " and resourcesIncludes "
						+ resourcesIncludes);
		return new ExportFromFilesystem(indexer, uploader, resourcesPath,
				resourcesExt, resourcesSyntax, resourcesIncludes);
	}

}
