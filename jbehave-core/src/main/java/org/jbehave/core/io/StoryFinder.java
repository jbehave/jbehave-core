package org.jbehave.core.io;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.DirectoryScanner;

import static java.util.Arrays.asList;

/**
 * Finds stories by scanning source paths, which can be either filesystem
 * directories or jars. Jars are identified by paths ending in ".jar".
 * 
 * Stories can be either in the form of class names or story paths.
 * 
 * Stories can be sorted by providing a sorting comparator.
 */
public class StoryFinder {

    private static final String JAR = ".jar";
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
     * Finds Java classes from a source path, allowing for includes/excludes,
     * and converts them to class names.
     * 
     * @param searchIn the path to search in
     * @param includes the List of include patterns, or <code>null</code> if
     *            none
     * @param excludes the List of exclude patterns, or <code>null</code> if
     *            none
     * @return A List of class names found
     */
    public List<String> findClassNames(String searchIn, List<String> includes, List<String> excludes) {
        return classNames(normalise(sort(scan(searchIn, includes, excludes))));
    }

    /**
     * Finds paths from a source URL, allowing for single include/exclude
     * pattern. Paths found are normalised by {@link
     * StoryFinder#normalise(List<String>)}
     * 
     * @param searchIn the source URL to search in
     * @param include the include pattern, or <code>""</code> if none
     * @param exclude the exclude pattern, or <code>""</code> if none
     * @return A List of paths found
     */
    public List<String> findPaths(URL searchIn, String include, String exclude) {
        return findPaths(CodeLocations.getPathFromURL(searchIn), asCSVList(include), asCSVList(exclude));
    }

    /**
     * Finds paths from a source URL, allowing for includes/excludes patterns.
     * Paths found are normalised by {@link StoryFinder#normalise(List<String>)}
     * 
     * @param searchIn the source URL to search in
     * @param includes the Array of include patterns, or <code>null</code> if
     *            none
     * @param excludes the Array of exclude patterns, or <code>null</code> if
     *            none
     * @return A List of paths found
     */
    public List<String> findPaths(URL searchIn, String[] includes, String[] excludes) {
        return findPaths(CodeLocations.getPathFromURL(searchIn), asList(includes), asList(excludes));
    }

    /**
     * Finds paths from a source path, allowing for include/exclude patterns,
     * which can be comma-separated values of multiple patterns.
     * 
     * Paths found are normalised by {@link StoryFinder#normalise(List<String>)}
     * 
     * @param searchIn the source path to search in
     * @param include the CSV include pattern, or <code>null</code> if none
     * @param exclude the CSV exclude pattern, or <code>null</code> if none
     * @return A List of paths found
     */
    public List<String> findPaths(String searchIn, String include, String exclude) {
        return findPaths(searchIn, asCSVList(include), asCSVList(exclude));
    }

    /**
     * Finds paths from a source path, allowing for include/exclude patterns.
     * Paths found are normalised by {@link StoryFinder#normalise(List<String>)}
     * 
     * @param searchIn the source path to search in
     * @param includes the Array of include patterns, or <code>null</code> if
     *            none
     * @param excludes the Array of exclude patterns, or <code>null</code> if
     *            none
     * @return A List of paths found
     */
    public List<String> findPaths(String searchIn, String[] includes, String[] excludes) {
        return findPaths(searchIn, asList(includes), asList(excludes));
    }

    /**
     * Finds paths from a source URL, allowing for includes/excludes patterns.
     * Paths found are normalised by {@link StoryFinder#normalise(List<String>)}
     * 
     * @param searchIn the source URL to search in
     * @param includes the List of include patterns, or <code>null</code> if
     *            none
     * @param excludes the List of exclude patterns, or <code>null</code> if
     *            none
     * @return A List of paths found
     */
    public List<String> findPaths(URL searchIn, List<String> includes, List<String> excludes) {
        return findPaths(CodeLocations.getPathFromURL(searchIn), includes, excludes);
    }

    /**
     * Finds paths from a source path, allowing for include/exclude patterns.
     * Paths found are normalised by {@link StoryFinder#normalise(List<String>)}
     * .
     * 
     * @param searchIn the source path to search in
     * @param includes the List of include patterns, or <code>null</code> if
     *            none
     * @param excludes the List of exclude patterns, or <code>null</code> if
     *            none
     * @return A List of paths found
     */
    public List<String> findPaths(String searchIn, List<String> includes, List<String> excludes) {
        return normalise(sort(scan(searchIn, includes, excludes)));
    }

    /**
     * Finds paths from a source path, allowing for includes/excludes. Paths
     * found are prefixed with specified path by {@link
     * StoryFinder#prefix(String, List<String>)} and normalised by {@link
     * StoryFinder#normalise(List<String>)}.
     * 
     * @param searchIn the source path to search in
     * @param includes the List of include patterns, or <code>null</code> if
     *            none
     * @param excludes the List of exclude patterns, or <code>null</code> if
     *            none
     * @param prefixWith the root path prefixed to all paths found, or
     *            <code>null</code> if none
     * @return A List of paths found
     */
    public List<String> findPaths(String searchIn, List<String> includes, List<String> excludes, String prefixWith) {
        return normalise(prefix(prefixWith, sort(scan(searchIn, includes, excludes))));
    }

    protected List<String> normalise(List<String> paths) {
        return paths.stream().map(path -> path.replace('\\', '/')).collect(Collectors.toList());
    }

    protected List<String> prefix(final String prefixWith, List<String> paths) {
        if (StringUtils.isBlank(prefixWith)) {
            return paths;
        }
        return paths.stream().map(prefixWith::concat).collect(Collectors.toList());
    }

    protected List<String> classNames(List<String> paths) {
        return paths.stream().map(path -> {
            if (!StringUtils.endsWithIgnoreCase(path, classNameExtension())) {
                return path;
            }
            return StringUtils.removeEndIgnoreCase(path, classNameExtension()).replace('/', '.');
        }).collect(Collectors.toList());
    }

    protected String classNameExtension() {
        return classNameExtension;
    }

    protected List<String> sort(List<String> input) {
        List<String> sorted = new ArrayList<>(input);
        Collections.sort(sorted, sortingComparator());
        return sorted;
    }

    /**
     * Comparator used for sorting. A <code>null</code> comparator means that
     * {@link Collections#sort()} will use natural ordering.
     * 
     * @return A Comparator or <code>null</code> for natural ordering.
     */
    protected Comparator<? super String> sortingComparator() {
        return sortingComparator;
    }

    protected List<String> scan(String source, List<String> includes, List<String> excludes) {
        if (source.endsWith(JAR)) {
            return scanJar(source, includes, excludes);
        }
        return scanDirectory(source, includes, excludes);
    }

    private List<String> asCSVList(String pattern) {
        List<String> list;
        if (pattern == null) {
            list = asList();
        } else {
            list = asList(pattern.split(","));
        }
        return list;
    }

    private List<String> scanDirectory(String basedir, List<String> includes, List<String> excludes) {
        DirectoryScanner scanner = new DirectoryScanner();
        if (!new File(basedir).exists()) {
            return new ArrayList<>();
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

    protected List<String> scanJar(String jarPath, List<String> includes, List<String> excludes) {
        return new JarFileScanner(jarPath, includes, excludes).scan();
    }

}
