package org.jbehave.core.io.rest;

import java.io.File;

public class FilesystemUtils {

	public static File asFile(Resource resource, String parentPath, String ext) {
		String childPath = ( resource.hasBreadcrumbs() ? resource.getBreadcrumbs() : "" ) + "/" + resource.getName() + ext;
		return new File(parentPath, childPath);
	}

}
