package org.jbehave.core.steps;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Provides parameter values as given types
 */
public interface Parameters extends Row {

    /**
     * Returns the value of a named parameter as a given type
     * 
     * @param type the Type or Class of type &lt;T&gt; to convert to
     * @param name the name of the parameter
     * @return The value of type &lt;T&gt;
     */
    <T> T valueAs(String name, Type type);

    /**
     * Returns the value of a named parameter as a given type while providing a
     * default value if the name is not found
     * 
     * @param type Type or Class of type &lt;T&gt; to convert to
     * @param name the name of the parameter
     * @param defaultValue the default value if the name is not found
     * @return The value of type &lt;T&gt;
     */
    <T> T valueAs(String name, Type type, T defaultValue);

    /**
     * Maps parameters to the specified type
     *
     * @param type The target type
     * @return The object of type &lt;T&gt;
     */
    <T> T mapTo(Class<T> type);

    /**
     * Maps parameters to the specified type
     *
     * @param type The target type
     * @param fieldNameMapping The field mapping between parameters and target type fields
     * @return The object of type &lt;T&gt;
     */
    <T> T mapTo(Class<T> type, Map<String, String> fieldNameMapping);
}
