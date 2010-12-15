package org.jbehave.core.model;

public interface Record {
    /**
     * Returns the value in a column.
     * 
     * @param name the name of the column.
     * @return the value.
     */
    String value(String name);

    /**
     * Returns {@code true} if this row contains the column.
     * 
     * @param name the name of the column.
     * @return {@code true} if this row contains the column.
     */
    boolean contains(String name);
}