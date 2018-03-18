package org.jbehave.core.embedder;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * EmbedderClassLoader is a URLClassLoader with a specified list of classpath
 * elements. It also provides a utility method
 * {@link #newInstance(Class, String)} to instantiate generic types.
 */
public class EmbedderClassLoader extends URLClassLoader {

    public EmbedderClassLoader(ClassLoader parent) {
        this(Arrays.<String>asList(), parent);
    }

    public EmbedderClassLoader(List<String> classpathElements) {
        this(classpathElements, Embedder.class.getClassLoader());
    }

    public EmbedderClassLoader(List<String> classpathElements, ClassLoader parent) {
        super(classpathURLs(classpathElements), parent);
    }

    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<T> type, String className) {
        try {
            Thread.currentThread().setContextClassLoader(this);
            return (T) loadClass(className, true).newInstance();
        } catch (Exception e) {
            throw new InstantiationFailed(className, type, this, e);
        }
    }

    public boolean isAbstract(String className) {
        try {
            return Modifier.isAbstract(loadClass(className, true).getModifiers());
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    List<String> asShortPaths(URL... urls) {
        List<String> names = new ArrayList<>();
        for (URL url : urls) {
            String path = url.getPath();
            if (isJar(path)) {
                names.add(shortPath(path));
            } else {
                names.add(path);
            }
        }
        return names;
    }

    private static String shortPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private static boolean isJar(String path) {
        return path.endsWith(".jar");
    }

    private static URL[] classpathURLs(List<String> elements) {
        List<URL> urls = new ArrayList<>();
        if (elements != null) {
            for (String element : elements) {
                urls.add(toURL(element));
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

    private static URL toURL(String element) {
        try {
            return new File(element).toURI().toURL();
        } catch ( Exception e ){
            throw new InvalidClasspathElement(element, e);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("urls", asShortPaths(getURLs()))
                .append("parent", getParent()).toString();
    }

    @SuppressWarnings("serial")
    public static class InstantiationFailed extends RuntimeException {

        public InstantiationFailed(String className, Class<?> type, ClassLoader classLoader, Throwable cause) {
            super("Instantiation failed for" + className + " of type " + type + " using class loader "+classLoader, cause);
        }

    }

    @SuppressWarnings("serial")
    public static class InvalidClasspathElement extends RuntimeException {

        public InvalidClasspathElement(String element, Throwable cause) {
            super("Invalid classpath element " + element, cause);
        }

    }
}
