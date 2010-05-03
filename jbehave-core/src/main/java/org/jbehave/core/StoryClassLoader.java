package org.jbehave.core;

import static java.util.Arrays.asList;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends URLClassLoader to instantiate {@link RunnableStory} instances.
 * 
 * @author Mauro Talevi
 */
public class StoryClassLoader extends URLClassLoader {

    public StoryClassLoader(List<String> classpathElements) throws MalformedURLException {
        super(classpathURLs(classpathElements), RunnableStory.class.getClassLoader());
    }

    public StoryClassLoader(List<String> classpathElements, ClassLoader parent) throws MalformedURLException {
        super(classpathURLs(classpathElements), parent);
    }

    /**
     * Loads and instantiates a runnable story class
     * 
     * @param storyClassName the name of the story class
     * @param parameterTypes the types of the constructor used to instantiate
     *            core class
     * @return A RunnableStory instance
     */
    public RunnableStory newStory(String storyClassName, Class<?>... parameterTypes) {
        try {
            Class<?> storyClass = loadStoryClass(storyClassName);
            RunnableStory story = newInstance(storyClass, parameterTypes);
            Thread.currentThread().setContextClassLoader(this);
            return story;
        } catch (ClassCastException e) {
            String message = "The story '" + storyClassName + "' must be of type '"
                    + RunnableStory.class.getName() + "'";
            throw new RuntimeException(message, e);
        } catch (Exception e) {
            String message = "The story '" + storyClassName + "' could not be instantiated with parameter types '"
                    + asList(parameterTypes) + "' and class loader '" + this + "'";
            throw new RuntimeException(message, e);
        }
    }

    public Class<?> loadStoryClass(String storyClassName) {
        try {
            return loadClass(storyClassName, true);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load class "+storyClassName, e);
        }
    }

    private RunnableStory newInstance(Class<?> storyClass, Class<?>... parameterTypes)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if ( parameterTypes != null && parameterTypes.length > 0 ){
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
        return "[" + StoryClassLoader.class.getName() + " urls=" + asShortPaths(getURLs()) + "]";
    }
}
