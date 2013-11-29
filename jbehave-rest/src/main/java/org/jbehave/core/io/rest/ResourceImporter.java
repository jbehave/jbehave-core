package org.jbehave.core.io.rest;

/**
 * Imports a resource index retrieved from the REST root URI. Each implementation
 * can import to different target systems, e.g. a filesystem.
 * 
 * @author Mauro Talevi
 */
public interface ResourceImporter {

    void importResources(String rootURI);

}
