package org.jbehave.core.steps;

import java.util.Map;

/**
 * Represents a row in an {@link ExamplesTable}. 
 */
public interface Row {

    /**
     * Returns the values as Strings
     * 
     * @return The Map of values
     */
    Map<String, String> values();

}