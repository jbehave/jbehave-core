package org.jbehave.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Extends URLClassLoader to instantiate {@link RunnableStory} instances.
 * 
 * @author Mauro Talevi
 */
public class StoryClassLoader extends URLClassLoader {

    public StoryClassLoader(List<String> classpathElements) throws MalformedURLException {
        this(classpathElements, RunnableStory.class.getClassLoader());
    }

    public StoryClassLoader(List<String> classpathElements, ClassLoader parent) throws MalformedURLException {
        super(classpathURLs(classpathElements), parent);
    }

    /**
     * Loads and instantiates a runnable story class
     * 
     * @param storyClassName
     *            the name of the story class
     * @param parameterTypes
     *            the types of the constructor used to instantiate core class
     * @return A RunnableStory instance
     */
    public RunnableStory newStory(String storyClassName, Class<?>... parameterTypes) {
        Class<?> storyClass = loadStoryClass(storyClassName);
        Thread.currentThread().setContextClassLoader(this);
        return instantiateStory(storyClass, parameterTypes);
    }

    public Class<?> loadStoryClass(String storyClassName) {
        try {
            return loadClass(storyClassName, true);
        } catch (ClassNotFoundException e) {
            throw new StoryClassNotFoundException(storyClassName, e);
        }
    }

    public RunnableStory instantiateStory(Class<?> storyClass, Class<?>... parameterTypes) {
        try {
            return newInstance(storyClass, parameterTypes);
        } catch (Exception e) {
            throw new StoryNotInstantiatedException(storyClass, Arrays.asList(parameterTypes), e);
        }
    }

    private RunnableStory newInstance(Class<?> storyClass, Class<?>... parameterTypes) throws NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        if (parameterTypes.length > 0 && parameterTypes[0].equals(ClassLoader.class)) {
            Constructor<?> constructor = storyClass.getConstructor(parameterTypes);
            return (RunnableStory) constructor.newInstance(this);
        }
        return (RunnableStory) storyClass.newInstance();
    }

    private List<String> asShortPaths(URL[] urls) {
        List<String> names = new ArrayList<String>();
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

    private static URL[] classpathURLs(List<String> elements) throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        if (elements != null) {
            for (String element : elements) {
                urls.add(new File(element).toURL());
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("urls", asShortPaths(getURLs()))
                .append("parent", getParent()).toString();
    }

    @SuppressWarnings("serial")
    public static class StoryClassNotFoundException extends RuntimeException {

        public StoryClassNotFoundException(String storyClassName, Throwable cause) {
            super("Story not found for class " + storyClassName, cause);
        }

    }

    @SuppressWarnings("serial")
    public static class StoryNotInstantiatedException extends RuntimeException {

        public StoryNotInstantiatedException(Class<?> storyClass, List<Class<?>> parameterTypes, Throwable cause) {
            super("Story not instantiated for class  " + storyClass + " and parameters  " + parameterTypes, cause);
        }

    }

}
