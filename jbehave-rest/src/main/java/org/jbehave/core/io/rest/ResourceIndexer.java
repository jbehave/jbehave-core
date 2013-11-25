package org.jbehave.core.io.rest;

import java.util.Map;


public interface ResourceIndexer {

    Map<String,Resource> indexResources(String rootPath);

}