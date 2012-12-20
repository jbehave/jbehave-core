package org.jbehave.core.io;

import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.removeStart;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
        String codeLocationPath = removeEnd(getPathFromURL(classResource), pathOfClass);
        if(codeLocationPath.endsWith(".jar!/")) {
            codeLocationPath=removeEnd(codeLocationPath,"!/");
        }
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
            return new File(filePath).toURI().toURL();
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

    /**
     * Get absolute path from URL objects starting with file:
     * This method takes care of decoding %-encoded chars, e.g. %20 -> space etc
     * Since we do not use a File object, the system specific path encoding
     * is not used (e.g. C:\ on Windows). This is necessary to facilitate
     * the removal of a class file with path in codeLocationFromClass 
     * 
     * @param url the file-URL
     * @return String absolute decoded path
     * @throws InvalidCodeLocation if URL contains format errors
     */
    public static String getPathFromURL(URL url) {
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            // this will probably not happen since the url was created
            // from a filename beforehand
            throw new InvalidCodeLocation(e.toString());
        }

        if(uri.toString().startsWith("file:") || uri.toString().startsWith("jar:")) {
            return removeStart(uri.getSchemeSpecificPart(),"file:");
        } else {
            // this is wrong, but should at least give a
            // helpful error when trying to open the file later
            return uri.toString();
        }
    }

}
