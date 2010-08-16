package org.jbehave.core.io;

import static org.apache.commons.lang.StringUtils.removeEnd;

import java.io.File;
import java.net.URL;

/**
 * Collection of utility methods to create code location URLs
 */
public class CodeLocations {

    /**
     * Creates a code location URL from a class
     * 
     * @param codeLocationClass the class
     * @return A URL created from Class
     * @throws InvalidCodeLocation if URL creation fails
     */
    public static URL codeLocationFromClass(Class<?> codeLocationClass) {
        String pathOfClass = codeLocationClass.getName().replace(".", "/") + ".class";
        URL classResource = codeLocationClass.getClassLoader().getResource(pathOfClass);
        String codeLocationPath = removeEnd(classResource.getFile(), pathOfClass);
        return codeLocationFromPath(codeLocationPath);
    }

    /**
     * Creates a code location URL from a file path
     * 
     * @param filePath the file path
     * @return A URL created from File
     * @throws InvalidCodeLocation if URL creation fails
     */
    public static URL codeLocationFromPath(String filePath) {
        try {
            return new File(filePath).toURL();
        } catch (Exception e) {
            throw new InvalidCodeLocation(filePath);
        }
    }

    /**
     * Creates a code location URL from a URL
     * 
     * @param url the URL external form
     * @return A URL created from URL
     * @throws InvalidCodeLocation if URL creation fails
     */
    public static URL codeLocationFromURL(String url) {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw new InvalidCodeLocation(url);
        }
    }

    @SuppressWarnings("serial")
    public static class InvalidCodeLocation extends RuntimeException {

        public InvalidCodeLocation(String path) {
            super(path);
        }

    }

}
