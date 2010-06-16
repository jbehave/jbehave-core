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
	
	    URL codeLocationURL = codeLocationClass.getProtectionDomain().getCodeSource().getLocation();
	    if (loadedByAnt(codeLocationURL)) {
			// Ant does some witchcraft in respect of Class.getProtectionDomain().getCodeSource()
			// returning $ANT_HOME/lib so we use the loaded resource file path
		    String pathOfClass = codeLocationClass.getName().replace(".", File.separator) + ".class";
	        URL loadedResource = codeLocationClass.getClassLoader().getResource(pathOfClass);
	        String codeLocationPath = removeEnd(loadedResource.getFile(), pathOfClass);
	        return codeLocationFromPath(codeLocationPath);
	    }
	    return codeLocationURL;
	}

	public static boolean loadedByAnt(URL codeLocationURL) {
		String externalForm = codeLocationURL.toExternalForm();
		return externalForm.contains("ant-1.") || externalForm.contains("ant.jar");
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
