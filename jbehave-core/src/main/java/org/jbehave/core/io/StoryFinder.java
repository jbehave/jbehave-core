package org.jbehave.core.io;

import static java.util.Arrays.asList;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Finds stories by scanning file system. Stories can be either in the form of
 * embeddable class names or story paths.
 */
public class StoryFinder {

    private static final String JAVA = ".java";
    private final String classNameExtension;
    private final Comparator<? super String> sortingComparator;

    public StoryFinder() {
        this(JAVA);
    }

    public StoryFinder(String classNameExtension) {
        this(classNameExtension, null);
    }

    public StoryFinder(Comparator<? super String> sortingComparator) {
        this(JAVA, sortingComparator);
    }

    private StoryFinder(String classNameExtension, Comparator<? super String> sortingComparator) {
        this.classNameExtension = classNameExtension;
        this.sortingComparator = sortingComparator;
    }

    /**
     * Finds java source paths from a base directory, allowing for
     * includes/excludes, and converts them to class names.
     * 
     * @param searchInDirectory
     *            the base directory path to search in
     * @param includes
     *            the List of include patterns, or <code>null</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>null</code> if none
     * @return A List of class names found
     */
    public List<String> findClassNames(String searchInDirectory, List<String> includes, List<String> excludes) {
        return classNames(normalise(sort(scan(searchInDirectory, includes, excludes))));
    }

    /**
     * Finds paths from a base URL, allowing for single include/exclude pattern. Paths
     * found are normalised by {@link StoryFinder#normalise(List<String>)}.
     * 
     * @param searchInURL
     *            the base URL to search in 
     * @param include
     *            the include pattern, or <code>""</code> if none
     * @param exclude
     *            the exclude pattern, or <code>""</code> if none
     * @return A List of paths found
     */
    public List<String> findPaths(URL searchInURL, String include, String exclude) {
        return findPaths(CodeLocations.getPathFromURL(searchInURL), asList(include), asList(exclude));
    }

    /**
     * Finds paths from a base directory, allowing for includes/excludes. Paths
     * found are normalised by {@link StoryFinder#normalise(List<String>)}.
     * 
     * @param searchInDirectory
     *            the base directory path to search in
     * @param includes
     *            the List of include patterns, or <code>null</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>null</code> if none
     * @return A List of paths found
     */
    public List<String> findPaths(String searchInDirectory, List<String> includes, List<String> excludes) {
        return normalise(sort(scan(searchInDirectory, includes, excludes)));
    }

    /**
     * Finds paths from a base directory, allowing for includes/excludes. Paths
     * found are prefixed with specified path by {@link
     * StoryFinder#prefix(String, List<String>)} and normalised by {@link
     * StoryFinder#normalise(List<String>)}.
     * 
     * @param searchInDirectory
     *            the base directory path to search in
     * @param includes
     *            the List of include patterns, or <code>null</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>null</code> if none
     * @param prefixWith
     *            the root path prefixed to all paths found, or
     *            <code>null</code> if none
     * @return A List of paths found
     */
    public List<String> findPaths(String searchInDirectory, List<String> includes, List<String> excludes,
            String prefixWith) {
        return normalise(prefix(prefixWith, sort(scan(searchInDirectory, includes, excludes))));
    }

    /**
     * Finds paths from a jar file, allowing for includes/excludes. Paths
     * found are normalised by {@link StoryFinder#normalise(List<String>)}.
     * 
     * @param jarPath
     *            the filename of the jar to search in
     * @param includes
     *            the List of include patterns, or <code>null</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>null</code> if none
     * @return A List of paths found
     */
    public List<String> findPathsFromJar(String jarPath, List<String> includes, List<String> excludes) {
        return findPathsFromJar(CodeLocations.codeLocationFromPath(jarPath), includes, excludes);
    }

    /**
     * Finds paths from a jar file, allowing for includes/excludes. Paths
     * found are normalised by {@link StoryFinder#normalise(List<String>)}.
     * 
     * @param jarPath
     *            the filename (URL) of the jar to search in
     * @param includes
     *            the List of include patterns, or <code>null</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>null</code> if none
     * @return A List of paths found
     */
    public List<String> findPathsFromJar(URL jarPath, List<String> includes, List<String> excludes) {
        return normalise(sort(new JarFileScanner(jarPath, includes, excludes).scan()));
    }

    /**
     * Finds paths from a jar file, allowing for an include/exclude. Paths
     * found are normalised by {@link StoryFinder#normalise(List<String>)}.
     * 
     * @param jarPath
     *            the filename of the jar to search in
     * @param includes
     *            the List of include patterns, or <code>""</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>""</code> if none
     * @return A List of paths found
     */
    public List<String> findPathsFromJar(String jarPath, String include, String exclude) {
        return findPathsFromJar(CodeLocations.codeLocationFromPath(jarPath), include, exclude);
    }

    /**
     * Finds paths from a jar file, allowing for includes/excludes. Paths
     * found are normalised by {@link StoryFinder#normalise(List<String>)}.
     * 
     * @param jarPath
     *            the filename (URL) of the jar to search in
     * @param includes
     *            the List of include patterns, or <code>""</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>""</code> if none
     * @return A List of paths found
     */
    public List<String> findPathsFromJar(URL jarPath, String include, String exclude) {
        return normalise(sort(new JarFileScanner(jarPath, include, exclude).scan()));
    }

    protected List<String> normalise(List<String> paths) {
        List<String> transformed = new ArrayList<String>(paths);
        CollectionUtils.transform(transformed, new Transformer() {
            public Object transform(Object input) {
                String path = (String) input;
                return path.replace('\\', '/');
            }
        });
        return transformed;
    }

    protected List<String> prefix(final String prefixWith, List<String> paths) {
        if (StringUtils.isBlank(prefixWith)) {
            return paths;
        }
        List<String> transformed = new ArrayList<String>(paths);
        CollectionUtils.transform(transformed, new Transformer() {
            public Object transform(Object input) {
                String path = (String) input;
                return prefixWith + path;
            }
        });
        return transformed;
    }

    protected List<String> classNames(List<String> paths) {
        List<String> trasformed = new ArrayList<String>(paths);
        CollectionUtils.transform(trasformed, new Transformer() {
            public Object transform(Object input) {
                String path = (String) input;
                if (!StringUtils.endsWithIgnoreCase(path, classNameExtension())) {
                    return input;
                }
                return StringUtils.removeEndIgnoreCase(path, classNameExtension()).replace('/', '.');
            }
        });
        return trasformed;
    }

    protected String classNameExtension() {
        return classNameExtension;
    }

    protected List<String> sort(List<String> input) {
        List<String> sorted = new ArrayList<String>(input);
        Collections.sort(sorted, sortingComparator());
        return sorted;
    }

    /**
     * Comparator used for sorting.  A <code>null</code> comparator means
     * that {@link Collections#sort()} will use natural ordering.
     * 
     * @return A Comparator or <code>null</code> for natural ordering.
     */
    protected Comparator<? super String> sortingComparator() {
        return sortingComparator;
    }

    protected List<String> scan(String basedir, List<String> includes, List<String> excludes) {
        DirectoryScanner scanner = new DirectoryScanner();
        if (!new File(basedir).exists()) {
            return new ArrayList<String>();
        }
        scanner.setBasedir(basedir);
        if (includes != null) {
            scanner.setIncludes(includes.toArray(new String[includes.size()]));
        }
        if (excludes != null) {
            scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
        }
        scanner.scan();
        return asList(scanner.getIncludedFiles());
    }

}
