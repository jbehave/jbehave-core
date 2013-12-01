package org.jbehave.core.io.rest;

import java.util.Map;

/**
 * Indexes the resources available from the REST root URI. The index is represented
 * as a map, indexed by the name of the resource.
 * 
 * @author Mauro Talevi
 */
public interface ResourceIndexer {

    /**
     * Indexes resources retrieved from the given root URI
     * 
     * @param rootURI the root URI
     * @return The Map of index Resource objects
     */
    Map<String, Resource> indexResources(String rootURI);

    /**
     * Indexes resources contained in the given index entity
     * 
     * @param rootURI the root URI
     * @param entity the entity containg the index
     * @return The Map of index Resource objects
     */
    Map<String, Resource> indexResources(String rootURI, String entity);

}