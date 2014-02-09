package org.jbehave.core.io.rest.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jbehave.core.io.rest.ResourceExporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.ResourceUploader;
import org.jbehave.core.io.rest.filesystem.ExportFromFilesystem;

/**
 * Mojo to export resources to REST root path from filesystem source path.
 * 
 * @goal export-from-filesystem
 * @requiresProject false
 */
public class ExportFromFilesystemMojo extends AbstractFilesystemMojo {

	/**
	 * The source path of the filesystem from which the resources are read
	 * 
	 * @parameter default-value="src/main/resources/stories"
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
	 * The includes pattern of the resources
	 * 
	 * @parameter default-value="**" expression="${jbehave.includes}
	 */
	String includes;

	public void execute() throws MojoExecutionException, MojoFailureException {
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
						+ restProvider + " with sourcePath " + sourcePath
						+ ", sourceExt " + sourceExt + " and including "
						+ includes);
		return new ExportFromFilesystem(indexer, uploader, sourcePath,
				sourceExt, includes);
	}

}
