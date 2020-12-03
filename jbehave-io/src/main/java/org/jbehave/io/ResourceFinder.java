package org.jbehave.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A utility class to retrieve resource, either from classpath or from
 * filesystem, and convert them to strings. Classpath resources are identified
 * by a configurable classpath prefix, which defaults to "classpath:". The
 * finder will resolve the resource path (i.e. stripping the prefix) and first
 * try to find the resource in the injected classloader, which default the
 * current thread context classloader, and failing that will fall back on
 * looking for the resource in the filesystem.
 */
public class ResourceFinder {

    public static final String DEFAULT_ROOT_DIRECTORY = "";
    public static final String DEFAULT_CLASSPATH_PREFIX = "classpath:";
	private final ClassLoader classLoader;
	private final String classpathPrefix;
	private String rootDirectory;

    public ResourceFinder() {
        this(DEFAULT_ROOT_DIRECTORY);
    }

    public ResourceFinder(String rootDirectory) {
		this(Thread.currentThread().getContextClassLoader(), rootDirectory,
				DEFAULT_CLASSPATH_PREFIX);
	}

	public ResourceFinder(ClassLoader classLoader, String rootDirectory) {
		this(classLoader, rootDirectory, DEFAULT_CLASSPATH_PREFIX);
	}

	public ResourceFinder(ClassLoader classLoader, String rootDirectory,
			String classpathPrefix) {
		this.classLoader = classLoader;
		this.classpathPrefix = classpathPrefix;
		this.rootDirectory = rootDirectory;
	}

	public String resourceAsString(String relativePath) {
		String resourcePath = resolvePath(relativePath);
		try {
			try {
				return classpathResource(resourcePath);
			} catch (ResourceNotFoundException e) {
				return filesystemResource(resourcePath);
			}
		} catch (IOException e) {
			throw new ResourceRetrievalFailedException(resourcePath);
		}

	}

	public void useRootDirectory(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	private String classpathResource(String resourcePath) throws IOException {
		InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
		if (inputStream != null) {
			return IOUtils.toString(inputStream, Charset.defaultCharset());
		}
		throw new ResourceNotFoundException(resourcePath, classLoader);
	}

	private String filesystemResource(String resourcePath) throws IOException {
		File file = new File(resourcePath);
		if (file.exists()) {
			return FileUtils.readFileToString(file, Charset.defaultCharset());
		}
		throw new ResourceNotFoundException(resourcePath);
	}

	private String resolvePath(String relativePath) {
		String resourcePath;
		if (rootDirectory.startsWith(classpathPrefix)) {
			resourcePath = resourcePath(stripPrefix(rootDirectory,
					classpathPrefix), relativePath);
		} else {
			resourcePath = resourcePath(rootDirectory, relativePath);

		}
		return resourcePath;
	}

	private String resourcePath(String rootDirectory, String relativePath) {
		if ( StringUtils.isBlank(rootDirectory) ){
			return relativePath;
		}
		return rootDirectory + "/" + relativePath;
	}

	private String stripPrefix(String path, String prefix) {
		return path.substring(prefix.length());
	}

	@SuppressWarnings("serial")
	public static class ResourceNotFoundException extends RuntimeException {

		public ResourceNotFoundException(String resourcePath) {
			super("Resource " + resourcePath + " not found");
		}

		public ResourceNotFoundException(String resourcePath,
				ClassLoader classLoader) {
			super("Resource " + resourcePath + " not found in classLoader "
					+ classLoader);
		}

	}

	@SuppressWarnings("serial")
	public static class ResourceRetrievalFailedException extends
			RuntimeException {

		public ResourceRetrievalFailedException(String resourcePath) {
			super("Failed to retrieve resource " + resourcePath);
		}

	}
}
