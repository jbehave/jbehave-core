package org.jbehave.core.io.odf;

import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromURL;

import static org.jbehave.core.io.odf.OdfUtils.loadOdt;
import static org.jbehave.core.io.odf.OdfUtils.parseOdt;

/**
 * Loads ODT story resources from URL
 */
public class LoadOdtFromURL extends LoadFromURL {

    public String loadResourceAsText(String resourcePath) {
        try {
            return parseOdt(loadOdt(resourceAsStream(resourcePath)));
        } catch (Exception cause) {
            throw new InvalidStoryResource(resourcePath, cause);
        }
    }

}