package org.jbehave.core.io.rest;

import java.util.Map;

/**
 * Indexes the resources available from the REST root URI. The index is represented
 * as a map, indexed by the name of the resource.
 * 
 * @author Mauro Talevi
 */
public interface ResourceIndexer {

    Map<String, Resource> indexResources(String rootURI);

}