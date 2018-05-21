package org.jbehave.core.parsers;

import java.util.List;

import org.jbehave.core.model.CompositeStep;

/**
 * <p>
 * Parses the composite steps from their textual representation.
 * </p>
 * @author Valery Yatsynovich
 */
public interface CompositeStepsParser {

    /**
     * Parses composite steps from their textual representation
     *
     * @param compositeStepsAsText the textual representation
     * @return The CompositeStep
     */
    List<CompositeStep> parseCompositeSteps(String compositeStepsAsText);
}
