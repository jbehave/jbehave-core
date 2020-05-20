package org.jbehave.core.steps;

import org.jbehave.core.model.ExamplesTable;

import java.util.Map;

/**
 * Represents a row in an {@link ExamplesTable}.
 */
public interface Row {

    /**
     * Returns the values as a Map, where the key is the column name and the value is the row value.
     * 
     * @return The Map of values
     */
    Map<String, String> values();

}