package org.jbehave.core.io.rest;

import static org.apache.commons.lang.StringUtils.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class IndexWithBreadcrumbs implements ResourceIndexer {

	private RESTClient client;

	public IndexWithBreadcrumbs(RESTClient client) {
		this.client = client;
	}

	public Map<String, Resource> indexResources(String rootURI) {
	    return indexResources(rootURI, get(uri(rootURI)));
	}

	public Map<String, Resource> indexResources(String rootURI, String entity) {
	    return createIndexWithBreadcrumbs(rootURI, entity);
	}

	protected Map<String, Resource> createIndexWithBreadcrumbs(String rootURI, String entity) {
		Map<String, Resource> index = createIndexFromEntity(rootURI, entity);
	    addBreadcrumbs(index);
	    return index;
	}

	protected void addBreadcrumbs(Map<String, Resource> index) {
	    for (Resource resource : index.values()) {
	        List<String> breadcrumbs = new ArrayList<String>();
	        collectBreadcrumbs(breadcrumbs, resource, index);
	        if (!breadcrumbs.isEmpty()) {
	            resource.setBreadcrumbs(join(breadcrumbs, "/"));
	        }
	    }
	}

	private void collectBreadcrumbs(List<String> breadcrumbs, Resource resource, Map<String, Resource> index) {
	    if (resource.hasParent()) {
	        String parentName = resource.getParentName();
	        breadcrumbs.add(0, parentName);
	        Resource parent = index.get(parentName);
	        if (parent != null) {
	            collectBreadcrumbs(breadcrumbs, parent, index);
	        }
	    }
	}
	
    private String get(String uri) {
        return client.get(uri);
    }

    protected abstract Map<String, Resource> createIndexFromEntity(String rootURI, String entity);

    protected abstract String uri(String rootPath);
    
}
