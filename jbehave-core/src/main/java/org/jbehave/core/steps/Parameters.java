package org.jbehave.core.steps;

/**
 * Provides parameter values as given types
 */
public interface Parameters extends Row {

    /**
     * Returns the value of a named parameter as a given type
     * 
     * @param type the Class of type <T> to convert to
     * @param name the name of the parameter
     * @return The value of type <T>
     */
    <T> T valueAs(String name, Class<T> type);

    /**
     * Returns the value of a named parameter as a given type while providing a
     * default value if the name is not found
     * 
     * @param type Class of type <T> to convert to
     * @param name the name of the parameter
     * @param defaultValue the default value if the name is not found
     * @return The value of type <T>
     */
    <T> T valueAs(String name, Class<T> type, T defaultValue);

}
