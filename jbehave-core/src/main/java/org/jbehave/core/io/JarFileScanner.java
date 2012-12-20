package org.jbehave.core.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.codehaus.plexus.util.SelectorUtils;

import static java.util.Arrays.asList;

/**
 * Find all matching file entries in a jar.
 */
public class JarFileScanner {

    private URL jarURL;
    private List<String> includes;
    private List<String> excludes;

    public JarFileScanner(String jarPath, String includes, String excludes) {
        this(CodeLocations.codeLocationFromPath(jarPath), includes, excludes);
    }

    public JarFileScanner(String jarPath, List<String> includes, List<String> excludes) {
        this(CodeLocations.codeLocationFromPath(jarPath), includes, excludes);
    }

    public JarFileScanner(URL jarURL, String includes, String excludes) {
        this(jarURL, asList(includes), asList(excludes));
    }

    public JarFileScanner(URL jarURL, List<String> includes, List<String> excludes) {
        this.jarURL = jarURL;
        this.includes = includes;
        this.excludes = excludes;
    }

    /**
     * Scans the jar file and returns the paths that match the includes and excludes.
     * 
     * @return A List of paths
     * @throws An IllegalStateException when the jar file is not found.
     */
    public List<String> scan() {
        try {
            JarFile jar = new JarFile(jarURL.getFile());
            try {
                List<String> result = new ArrayList<String>();
                Enumeration<JarEntry> en = jar.entries();
                while (en.hasMoreElements()) {
                    JarEntry entry = en.nextElement();
                    String path = entry.getName();
                    String pathLocal = localPathFormat(path);
                    boolean match = includes.size() == 0;
                    if (!match) {
                        for (String pattern : includes) {
                            if (SelectorUtils.matchPath(pattern, pathLocal)) {
                                match = true;
                                break;
                            }
                        }
                    }
                    if (match) {
                        for (String pattern : excludes) {
                            if (SelectorUtils.matchPath(pattern, pathLocal)) {
                                match = false;
                                break;
                            }
                        }
                    }
                    if (match) {
                        result.add(path);
                    }
                }
                return result;
            } finally {
                jar.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // SelectorUtils assumes local path separator for path and pattern
    private String localPathFormat(String path) {
        return path.replace('/', File.separatorChar);
    }

}
