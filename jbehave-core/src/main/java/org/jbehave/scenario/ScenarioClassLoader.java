package org.jbehave.scenario;

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
 * Extends URLClassLoader to instantiate scenarios.
 * 
 * @author Mauro Talevi
 */
public class ScenarioClassLoader extends URLClassLoader {

    public ScenarioClassLoader(List<String> classpathElements) throws MalformedURLException {
        super(classpathURLs(classpathElements), RunnableScenario.class.getClassLoader());
    }

    public ScenarioClassLoader(List<String> classpathElements, ClassLoader parent) throws MalformedURLException {
        super(classpathURLs(classpathElements), parent);
    }

    /**
     * Loads and instantiates a runnable scenario class
     * 
     * @param scenarioClassName the name of the scenario class
     * @param parameterTypes the types of the constructor used to instantiate
     *            scenario class
     * @return A RunnableScenario instance
     */
    public RunnableScenario newScenario(String scenarioClassName, Class<?>... parameterTypes) {
        try {
            Class<?> scenarioClass = loadClass(scenarioClassName, true);
            RunnableScenario scenario = newInstance(scenarioClass, parameterTypes);
            Thread.currentThread().setContextClassLoader(this);
            return scenario;
        } catch (ClassCastException e) {
            String message = "The scenario '" + scenarioClassName + "' must be of type '"
                    + RunnableScenario.class.getName() + "'";
            throw new RuntimeException(message, e);
        } catch (Exception e) {
            String message = "The scenario '" + scenarioClassName + "' could not be instantiated with parameter types '"
                    + asList(parameterTypes) + "' and class loader '" + this + "'";
            throw new RuntimeException(message, e);
        }
    }

    private RunnableScenario newInstance(Class<?> scenarioClass, Class<?>... parameterTypes)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if ( parameterTypes != null && parameterTypes.length > 0 ){
            Constructor<?> constructor = scenarioClass.getConstructor(parameterTypes);
            return (RunnableScenario) constructor.newInstance(this);            
        }
        return (RunnableScenario) scenarioClass.newInstance();
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
        return "[" + ScenarioClassLoader.class.getName() + " urls=" + asShortPaths(getURLs()) + "]";
    }
}
