package org.jbehave.core.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;

/**
 * Finds story paths from a filesystem.
 * 
 * @author Mauro Talevi
 */
public class StoryPathFinder {

    private static final String JAVA = ".java";
    private static final String EMPTY = "";
    private static final String DOT_REGEX = "\\.";
    private static final String SLASH = "/";
	private static final String BACKSLASH = "\\\\";

    private DirectoryScanner scanner = new DirectoryScanner();

    /**
     * Lists story paths from a base directory, allowing for includes/excludes.
     * If Java paths are found, these are normalised to Java class names.
     * 
     * @param basedir the base directory path
     * @param rootPath the root path prefixed to all paths found, or
     *            <code>null</code> if none
     * @param includes the List of include patterns, or <code>null</code> if
     *            none
     * @param excludes the List of exclude patterns, or <code>null</code> if
     *            none
     * @return A List of paths
     */
    public List<String> listStoryPaths(String basedir, String rootPath, List<String> includes,
            List<String> excludes) {
        List<String> paths = new ArrayList<String>();
        for (String path : listPaths(basedir, rootPath, includes, excludes)) {
            paths.add(normalise(path));
        }
        return paths;
    }

    private String normalise(String path) {
        if ( path.indexOf(JAVA) != -1 ){
            String className = path.substring(0, path.indexOf(JAVA));
            className = className.replaceAll(SLASH, DOT_REGEX); 
            return className.replaceAll(BACKSLASH, DOT_REGEX);            
        } else {
            if ( path.startsWith("/") ){
                return path.substring(1);
            }           
            return path;

        }
    }

    private List<String> listPaths(String basedir, String rootPath, List<String> includes, List<String> excludes) {
        List<String> paths = new ArrayList<String>();
        if ( !new File(basedir).exists() ){
            return paths;
        }
        scanner.setBasedir(basedir);
        if (includes != null) {
            scanner.setIncludes(includes.toArray(new String[includes.size()]));
        }
        if (excludes != null) {
            scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
        }
        scanner.scan();
        String basePath = (rootPath != null ? rootPath + SLASH : EMPTY);
        for (String relativePath : scanner.getIncludedFiles()) {
            String path = basePath + relativePath;
            paths.add(path);
        }
        return paths;
    }

}
