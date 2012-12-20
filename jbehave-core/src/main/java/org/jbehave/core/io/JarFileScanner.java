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

import org.codehaus.plexus.util.SelectorUtils;

/**
 * Find all matching file entries in a jar.
 * This class should be able to read local files as well as URLs, (URL is not yet
 * tested however)
 *
 * The class throws a IllegalStateException instead of e.g. FileNotFoundException
 * when the jar file is not found since this is more convenient in the findPath
 * methods in StoryFinder (the same behavior is present in the DirectoryScanner
 * from plexus util)
 */
public class JarFileScanner {

    private URL jarPath;
    private List<String> includes;
    private List<String> excludes;

    private JarFileScanner(URL pathname) {
        this.jarPath=pathname;
        this.includes=new ArrayList<String>();
        this.excludes=new ArrayList<String>();
    }

    private void setIncludes(List<String> includes) {
        this.includes=localPathFormat(includes);
    }

    private void setExcludes(List<String> excludes) {
        this.excludes=localPathFormat(excludes);
    }

    private List<String> scan() {
        try {
            JarFile jar = new JarFile(jarPath.getFile());
            try {
                List<String> result=new ArrayList<String>();
                Enumeration<JarEntry> en=jar.entries();
                while(en.hasMoreElements()) {
                    JarEntry entry=en.nextElement();
                    String path=entry.getName();
                    String pathLocal=localPathFormat(path);
                    boolean match=includes.size()==0;
                    if(!match) {
                        for(String pattern: includes) {
                            if(SelectorUtils.matchPath(pattern, pathLocal)) {
                                match=true;
                                break;
                            }
                        }
                    }
                    if(match) {
                        for(String pattern: excludes) {
                            if(SelectorUtils.matchPath(pattern, pathLocal)) {
                                match=false;
                                break;
                            }
                        }
                    }
                    if(match) {
                        result.add(path);
                    }
                }
                return result;
            }
            finally {
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

    private List<String> localPathFormat(List<String> paths) {
        List<String> result=new ArrayList<String>();
        for(String entry: paths) {
            result.add(localPathFormat(entry));
        }
        return result;
    }

    public static List<String> scanJar(URL pathname, List<String> includes, List<String> excludes) {
        JarFileScanner scanner=new JarFileScanner(pathname);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        return scanner.scan();
    }

    public static List<String> scanJar(String pathname, List<String> includes, List<String> excludes) {
        return scanJar(CodeLocations.codeLocationFromPath(pathname), includes, excludes);
    }

    public static List<String> scanJar(URL pathname, String include, String exclude) {
        return scanJar(pathname, Arrays.asList(include), Arrays.asList(exclude));
    }

    public static List<String> scanJar(String pathname, String include, String exclude) {
        return scanJar(CodeLocations.codeLocationFromPath(pathname), include, exclude);
    }

}
