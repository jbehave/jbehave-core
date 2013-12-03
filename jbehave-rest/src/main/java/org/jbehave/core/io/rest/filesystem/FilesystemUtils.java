package org.jbehave.core.io.rest.filesystem;

import java.io.File;

import org.jbehave.core.io.rest.Resource;

public class FilesystemUtils {

	public static File asFile(Resource resource, String parentPath, String ext) {
		String childPath = ( resource.hasBreadcrumbs() ? resource.getBreadcrumbs() : "" ) + "/" + resource.getName() + ext;
		return new File(parentPath, childPath);
	}

}
