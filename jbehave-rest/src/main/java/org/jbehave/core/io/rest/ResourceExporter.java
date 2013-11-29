package org.jbehave.core.io.rest;

/**
 * Exports resources to a REST root URI. Each implementation
 * can export from different source systems, e.g. a filesystem.
 * 
 * @author Mauro Talevi
 */
public interface ResourceExporter {

    void exportResources(String rootURI);

}
