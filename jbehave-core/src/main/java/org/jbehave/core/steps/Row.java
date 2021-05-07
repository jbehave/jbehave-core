package org.jbehave.core.steps;

import java.util.Map;

import org.jbehave.core.model.ExamplesTable;

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