package org.jbehave.core.parsers;

import java.util.List;

import org.jbehave.core.model.Composite;

/**
 * <p>
 * Parses the composite steps from their textual representation.
 * </p>
 * @author Valery Yatsynovich
 */
public interface CompositeParser {

    /**
     * Parses composite steps from their textual representation
     *
     * @param compositesAsText the textual representation
     * @return The List of Composite
     */
    List<Composite> parseComposites(String compositesAsText);
}
