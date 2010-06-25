package org.jbehave.core.io;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;

/**
 * Finds story paths from a file system, using Ant's {@link DirectoryScanner}.
 */
public class StoryPathFinder {

    private final DirectoryScanner scanner;

    public StoryPathFinder() {
        this(new DirectoryScanner());
    }

    public StoryPathFinder(DirectoryScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Finds paths from a base directory, allowing for includes/excludes. Paths
     * found are prefixed with specified path by {@link
     * StoryPathFinder#prefix(String, List<String>)} and normalised by {@link
     * StoryPathFinder#normalise(List<String>)}.
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
        return normalise(prefix(prefixWith, scan(searchInDirectory, includes, excludes)));
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

    protected List<String> scan(String basedir, List<String> includes, List<String> excludes) {
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
