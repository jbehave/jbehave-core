package org.jbehave.core.io.rest;

import static java.text.MessageFormat.format;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;
import static org.jbehave.core.io.rest.filesystem.FilesystemUtils.fileNameWithoutExt;
import static org.jbehave.core.io.rest.filesystem.FilesystemUtils.normalisedPathOf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.io.StoryFinder;

public abstract class IndexWithBreadcrumbs implements ResourceIndexer {

	private static final String EMPTY = "";
	private static final String FULL_PATH = "{0}/{1}";
	
	private RESTClient client;
	private ResourceNameResolver nameResolver;

	public IndexWithBreadcrumbs(RESTClient client,
			ResourceNameResolver nameResolver) {
		this.client = client;
		this.nameResolver = nameResolver;
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
				codeLocationFromPath(rootPath), includes, EMPTY);
		for (String path : paths) {
			addPath(rootURI, rootPath, fullPath(rootPath, path), index);
		}
		return index;
	}

	private void addPath(String rootURI, String rootPath, String path,
			Map<String, Resource> index) {
		File file = new File(path);
		String name = resolveName(fileNameWithoutExt(file));
		String parentName = parentName(file, rootPath);
		String uri = fullPath(rootURI, name);
		Resource resource = new Resource(uri, name, parentName);
		resource.setContent(contentOf(file));
		index.put(name, resource);
		if (parentName != null) {
			addPath(rootURI, rootPath, file.getParent(), index);
		}
	}

	private String parentName(File file, String rootPath) {
		File parent = file.getParentFile();
		if (parent != null && !normalisedPathOf(parent).equals(rootPath)) {
			return resolveName(parent.getName());
		}
		return null;
	}

	private String fullPath(String root, String path) {
		return format(FULL_PATH, root,  path);
	}

	private String contentOf(File file) {
		if (file.isDirectory()) {
			return EMPTY;
		}
		try {
			return FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to read content of file " + file, e);
		}
	}

	protected void addBreadcrumbs(Map<String, Resource> index) {
		for (Resource resource : index.values()) {
			List<String> breadcrumbs = new ArrayList<String>();
			collectBreadcrumbs(breadcrumbs, resource, index);
			resource.setBreadcrumbs(breadcrumbs);
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

	protected String resolveName(String input){
		return nameResolver.resolve(input);
	}
	
	public static class ToLowerCase implements ResourceNameResolver {

		public String resolve(String input) {
			return input.toLowerCase();
		}

	}
}
