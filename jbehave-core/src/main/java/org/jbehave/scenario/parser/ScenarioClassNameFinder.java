package org.jbehave.scenario.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.jbehave.scenario.errors.InvalidScenarioClassPathException;

/**
 * Finds scenario class names from a base directory using Ant's directory scanner.
 * 
 * @author Mauro Talevi
 */
public class ScenarioClassNameFinder {

    private static final String JAVA = ".java";
    private static final String EMPTY = "";
    private static final String DOT_REGEX = "\\.";
    private static final String SLASH = "/";
	private static final String BACKSLASH = "\\\\";

    private DirectoryScanner scanner = new DirectoryScanner();

    /**
     * Lists scenario class names from a base directory, allowing for includes/excludes
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
    public List<String> listScenarioClassNames(String basedir, String rootPath, List<String> includes,
            List<String> excludes) {
        List<String> classNames = new ArrayList<String>();
        for (String path : listPaths(basedir, rootPath, includes, excludes)) {
            classNames.add(classNameFor(path));
        }
        return classNames;
    }

    private String classNameFor(String path) {
        int javaPath = path.indexOf(JAVA);
        if ( javaPath != -1 ){
            String className = path.substring(0, javaPath);
            className = className.replaceAll(SLASH, DOT_REGEX); 
            return className.replaceAll(BACKSLASH, DOT_REGEX);            
        }
        throw new InvalidScenarioClassPathException("Invalid scenario class path "+path);
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
