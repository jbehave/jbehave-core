package org.jbehave.core.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.codehaus.plexus.util.SelectorUtils;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
/**
 * Find all matching file entries in a jar.
 */
public class JarFileScanner {

    private URL jarURL;
    private List<String> includes;
    private List<String> excludes;

    public JarFileScanner(String jarPath, String includes, String excludes) {
        this(jarPath, asList(includes), asList(excludes));
    }

    public JarFileScanner(String jarPath, List<String> includes, List<String> excludes) {
        this(CodeLocations.codeLocationFromPath(jarPath), includes, excludes);
    }

    public JarFileScanner(URL jarURL, String includes, String excludes) {
        this(jarURL, asList(includes), asList(excludes));
    }

    public JarFileScanner(URL jarURL, List<String> includes, List<String> excludes) {
        this.jarURL = jarURL;
        this.includes = ( includes != null ? toLocalPath(includes) : Arrays.<String>asList() );
        this.excludes = ( excludes != null ? toLocalPath(excludes) : Arrays.<String>asList() );
    }

    /**
     * Scans the jar file and returns the paths that match the includes and excludes.
     * 
     * @return A List of paths
     * @throws An IllegalStateException when an I/O error occurs in reading the jar file.
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
                    boolean match = includes.size() == 0;
                    if (!match) {
                        for (String pattern : includes) {
                            if ( patternMatches(pattern, path)) {
                                match = true;
                                break;
                            }
                        }
                    }
                    if (match) {
                        for (String pattern : excludes) {
                            if ( patternMatches(pattern, path)) {
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

    private List<String> toLocalPath(List<String> patternList) {
        List<String> transformed = new ArrayList<String>(patternList);
        CollectionUtils.transform(transformed, new Transformer<String, String>() {
            public String transform(String pattern) {
                return pattern!=null ? pattern.replace('/', File.separatorChar) : null;
            }
        });
        return transformed;
    }

    private boolean patternMatches(String pattern, String path) {
        if ( isBlank(pattern) ) return false;
        // SelectorUtils assumes local path separator for path and pattern
        String localPath = path.replace('/', File.separatorChar);
        return SelectorUtils.matchPath(pattern, localPath);
    }

}
