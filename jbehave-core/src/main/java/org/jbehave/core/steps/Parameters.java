package org.jbehave.core.steps;


public interface Parameters extends Row {

    /**
     * Determines if the parameter of a given name is found
     * 
     * @param name the name of the parameter
     * @return A boolean, <code>true</code> if found
     */
    boolean hasValue(String name);

    /**
     * Returns the value for parameter of a given name
     * 
     * @param type the Class of type <T> to convert to
     * @param name the name of the parameter
     * @return The value of type <T>
     */
    <T> T valueAs(String name, Class<T> type);

    /**
     * Returns the value for parameter of a given name and provides a default
     * value if the name is not found
     * 
     * @param type Class of type <T> to convert to
     * @param name the name of the parameter
     * @param defaultValue the default value if the name doesn't exist.
     * @return The value of type <T>
     */
    <T> T valueAs(String name, Class<T> type, T defaultValue);

}