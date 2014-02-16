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
     * Indexes resources found in the given root path
     * 
     * @param rootURI the root URI
     * @param rootPath the root path of the resources to index
     * @param syntax the syntax of the resources to index.  Can be <code>null</code> or blank.
     * @param includes the Ant-like pattern of the paths to index.  Can be <code>null</code> or blank. 
     * @return The Map of index Resource objects
     */
    Map<String, Resource> indexResources(String rootURI, String rootPath, String syntax, String includes);

}