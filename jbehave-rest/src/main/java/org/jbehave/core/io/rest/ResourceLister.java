package org.jbehave.core.io.rest;

import java.util.List;

public interface ResourceLister {

    List<String> listResources(String rootPath);

}