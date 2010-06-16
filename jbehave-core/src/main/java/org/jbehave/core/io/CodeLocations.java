package org.jbehave.core.io;

import static org.apache.commons.lang.StringUtils.removeEnd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Collection of utility methods to create code location URLs
 */
public class CodeLocations {

	public static URL codeLocationFromClass(Class<?> codeLocationClass) {	
	    URL codeLocationURL = codeLocationFromClassCodeSource(codeLocationClass);
	    if (isAntJar(codeLocationURL)) {
			// Ant returns the path to ant.jar as CodeSource.getLocation()
			// so we use the class resource path
		    codeLocationURL = codeLocationFromClassResourcePath(codeLocationClass);
	    }
	    return codeLocationURL;
	}

	public static boolean isAntJar(URL codeLocationURL) {
		String externalForm = codeLocationURL.toExternalForm();
		return externalForm.contains("ant-1.") || externalForm.contains("ant.jar");
	}

	public static URL codeLocationFromClassCodeSource(Class<?> codeLocationClass) {
		return codeLocationClass.getProtectionDomain().getCodeSource().getLocation();
	}

	public static URL codeLocationFromClassResourcePath(Class<?> codeLocationClass) {
		String pathOfClass = codeLocationClass.getName().replace(".", File.separator) + ".class";
		URL loadedResource = codeLocationClass.getClassLoader().getResource(pathOfClass);
		String codeLocationPath = removeEnd(loadedResource.getFile(), pathOfClass);
		return codeLocationFromPath(codeLocationPath);
	}

	public static URL codeLocationFromPath(String codeLocationPath) {
		try {
		    return new File(codeLocationPath).toURL();
		} catch (MalformedURLException e) {
		    throw new InvalidCodeLocation(codeLocationPath);
		}
	}

	@SuppressWarnings("serial")
	public static class InvalidCodeLocation extends RuntimeException {
	
		public InvalidCodeLocation(String path) {
			super(path);
		}
		
	}


}
