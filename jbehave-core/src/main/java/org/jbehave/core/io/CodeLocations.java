package org.jbehave.core.io;

import static org.apache.commons.lang.StringUtils.removeEnd;

import java.io.File;
import java.net.URL;

/**
 * Collection of utility methods to create code location URLs
 */
public class CodeLocations {

	public static URL codeLocationFromClass(Class<?> codeLocationClass) {	
		String pathOfClass = codeLocationClass.getName().replace(".", "/") + ".class";
		URL classResource = codeLocationClass.getClassLoader().getResource(pathOfClass);
		String codeLocationPath = removeEnd(classResource.getFile(), pathOfClass);
		return codeLocationFromPath(codeLocationPath);
	}

	public static URL codeLocationFromPath(String codeLocationPath) {
		try {
		    return new File(codeLocationPath).toURL();
		} catch (Exception e) {
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
