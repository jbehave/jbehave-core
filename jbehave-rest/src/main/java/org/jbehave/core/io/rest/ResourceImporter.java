package org.jbehave.core.io.rest;

/**
 * Imports a resource index retrieved from the source path. Each implementation
 * can import to different target systems, e.g. a filesystem.
 * 
 * @author Mauro Talevi
 */
public interface ResourceImporter {

    void importResources(String sourcePath);

}
