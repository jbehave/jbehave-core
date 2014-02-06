package org.jbehave.core.io.rest;

import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;

public abstract class IndexWithBreadcrumbs implements ResourceIndexer {

	private RESTClient client;

	public IndexWithBreadcrumbs(RESTClient client) {
		this.client = client;
	}

	public Map<String, Resource> indexResources(String rootURI) {
		String entity = get(uri(rootURI));
		Map<String, Resource> index = createIndexFromEntity(rootURI, entity);
		addBreadcrumbs(index);
		return index;
	}

	public Map<String, Resource> indexResources(String rootURI,
			String rootPath, String includes) {
		Map<String, Resource> index = createIndexFromPaths(rootURI, rootPath,
				includes);
		addBreadcrumbs(index);
		return index;
	}

	protected Map<String, Resource> createIndexFromPaths(String rootURI,
			String rootPath, String includes) {
		Map<String, Resource> index = new HashMap<String, Resource>();
		List<String> paths = new StoryFinder().findPaths(
				CodeLocations.codeLocationFromPath(rootPath), includes, "");
		for (String path : paths) {
			addPath(rootURI, rootPath, rootPath + "/" + path, index);
		}
		return index;
	}

	private void addPath(String rootURI, String rootPath, String path,
			Map<String, Resource> index) {
		File file = new File(path);
		String name = substringBeforeLast(file.getName(), ".").toLowerCase();
		String parentName = parentName(file, rootPath);
		String uri = rootURI + "/" + name;
		Resource resource = new Resource(uri, name, parentName);
		resource.setText(textOf(file));
		index.put(name, resource);
		if ( parentName != null ) {
			addPath(rootURI, rootPath, file.getParent(), index);
		}
	}

	private String parentName(File file, String rootPath) {
		File parent = file.getParentFile();
		if (parent != null && !parent.getPath().equals(rootPath)) {
			return parent.getName().toLowerCase();
		}
		return null;
	}

	private String textOf(File file) {
		if (file.isDirectory()) {
			return "";
		}
		try {
			return FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read file " + file, e);
		}
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

	private void collectBreadcrumbs(List<String> breadcrumbs,
			Resource resource, Map<String, Resource> index) {
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

	protected abstract Map<String, Resource> createIndexFromEntity(
			String rootURI, String entity);

	protected abstract String uri(String rootPath);

}
