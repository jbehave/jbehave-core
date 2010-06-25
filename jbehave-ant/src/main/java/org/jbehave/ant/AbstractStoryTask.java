package org.jbehave.ant;

import static java.util.Arrays.asList;
import static org.apache.tools.ant.Project.MSG_DEBUG;
import static org.apache.tools.ant.Project.MSG_INFO;
import static org.apache.tools.ant.Project.MSG_WARN;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jbehave.core.RunnableStory;
import org.jbehave.core.StoryClassLoader;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.UnmodifiableEmbedderControls;
import org.jbehave.core.io.PathToClassNames;
import org.jbehave.core.io.StoryPathFinder;

/**
 * Abstract task that holds all the configuration parameters to specify and load
 * stories.
 * 
 * @author Mauro Talevi
 */
public abstract class AbstractStoryTask extends Task {

	private static final String TEST_SCOPE = "test";

	private String sourceDirectory = "src/main/java";

	private String testSourceDirectory = "src/test/java";

	/**
	 * The scope of the source, either "compile" or "test"
	 */
	private String scope = "compile";

	/**
	 * Story class names, if specified take precedence over the names specificed
	 * via the "storyIncludes" and "storyExcludes" parameters
	 */
	private List<String> storyClassNames = new ArrayList<String>();

	/**
	 * Story include filters, relative to the root source directory determined
	 * by the scope
	 */
	private List<String> storyIncludes = new ArrayList<String>();

	/**
	 * Story exclude filters, relative to the root source directory determined
	 * by the scope
	 */
	private List<String> storyExcludes = new ArrayList<String>();

	/**
	 * The boolean flag to determined if class loader is injected in story
	 * classes
	 */
	private boolean classLoaderInjected = false;

	/**
	 * The boolean flag to skip running stories
	 */
	private boolean skip = false;

	/**
	 * The boolean flag to ignore failure in stories
	 */
	private boolean ignoreFailureInStories = false;

	/**
	 * The boolean flag to ignore failure in view
	 */
	private boolean ignoreFailureInView = false;

	/**
	 * The boolean flag to generate view after stories
	 */
	private boolean generateViewAfterStories = true;

	/**
	 * The boolean flag to run in batch mode
	 */
	private boolean batch = false;

	/**
	 * The embedder to run the stories
	 */
	private String embedderClass = Embedder.class.getName();

	/**
	 * Used to find story paths
	 */
	private StoryPathFinder finder = new StoryPathFinder();

	/**
	 * Determines if the scope of the source directory is "test"
	 * 
	 * @return A boolean <code>true</code> if test scoped
	 */
	private boolean isSourceTestScope() {
		return TEST_SCOPE.equals(scope);
	}

	private String rootSourceDirectory() {
		if (isSourceTestScope()) {
			return testSourceDirectory;
		}
		return sourceDirectory;
	}

	private List<String> findStoryClassNames() {
		log("Searching for story class names including " + storyIncludes
				+ " and excluding " + storyExcludes, MSG_DEBUG);
		List<String> storyClassNames =  new PathToClassNames().transform(finder.findPaths(
				rootSourceDirectory(), storyIncludes, storyExcludes, null));
		log("Found story class names: " + storyClassNames, MSG_DEBUG);
		return storyClassNames;
	}

	/**
	 * Creates the Story ClassLoader with the classpath element of the selected
	 * scope
	 * 
	 * @return A StoryClassLoader
	 * @throws MalformedURLException
	 */
	protected StoryClassLoader createStoryClassLoader()
			throws MalformedURLException {
		return new StoryClassLoader(asList(new String[] {}));
	}

	protected EmbedderMonitor embedderMonitor() {
		return new AntEmbedderMonitor();
	}

	protected EmbedderControls embedderControls() {
		return new UnmodifiableEmbedderControls(
				new EmbedderControls().doBatch(batch).doSkip(skip)
				.doGenerateViewAfterStories(generateViewAfterStories)
				.doIgnoreFailureInStories(ignoreFailureInStories)
				.doIgnoreFailureInView(ignoreFailureInView));
	}

	protected List<String> storyPaths() {
		log("Searching for story paths including " + storyIncludes
				+ " and excluding " + storyExcludes, MSG_DEBUG);
		List<String> storyPaths = finder.findPaths(rootSourceDirectory(),
				storyIncludes, storyExcludes, null);
		log("Found story paths: " + storyPaths, MSG_DEBUG);
		return storyPaths;
	}

	/**
	 * Returns the list of story instances, whose class names are either
	 * specified via the parameter "storyClassNames" (which takes precedence) or
	 * found using the parameters "storyIncludes" and "storyExcludes".
	 * 
	 * @return A List of RunnableStories
	 * @throws BuildException
	 */
	protected List<RunnableStory> stories() throws BuildException {
		List<String> names = storyClassNames;
		if (names == null || names.isEmpty()) {
			names = findStoryClassNames();
		}
		if (names.isEmpty()) {
			log("No stories to run.", MSG_INFO);
		}
		StoryClassLoader classLoader = null;
		try {
			classLoader = createStoryClassLoader();
		} catch (Exception e) {
			throw new BuildException("Failed to create story class loader", e);
		}
		List<RunnableStory> stories = new ArrayList<RunnableStory>();
		for (String name : names) {
			try {
				if (!isStoryAbstract(classLoader, name)) {
					stories.add(storyFor(classLoader, name));
				}
			} catch (Exception e) {
				throw new BuildException("Failed to instantiate core '" + name
						+ "'", e);
			}
		}
		return stories;
	}

	private boolean isStoryAbstract(StoryClassLoader classLoader, String name)
			throws ClassNotFoundException {
		return Modifier.isAbstract(classLoader.loadClass(name).getModifiers());
	}

	private RunnableStory storyFor(StoryClassLoader classLoader, String name) {
		if (classLoaderInjected) {
			try {
				return classLoader.newStory(name, ClassLoader.class);
			} catch (RuntimeException e) {
				throw new RuntimeException(
						"JBehave is trying to instantiate your Story class '"
								+ name
								+ "' with a ClassLoader as a parameter.  "
								+ "If this is wrong, change the Ant configuration for the plugin to include "
								+ "<classLoaderInjected>false</classLoaderInjected>",
						e);
			}
		}
		return classLoader.newStory(name);
	}

	protected Embedder newEmbedder() {
		try {
			Embedder embedder = (Embedder) createStoryClassLoader()
					.loadClass(embedderClass).newInstance();
			embedder.useEmbedderMonitor(embedderMonitor());
			embedder.useEmbedderControls(embedderControls());
			return embedder;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create embedder "
					+ embedderClass, e);
		}
	}

	protected class AntEmbedderMonitor implements EmbedderMonitor {
		public void storiesBatchFailed(String failedStories) {
			log("Failed to run stories batch: " + failedStories, MSG_WARN);
		}

		public void storyFailed(String storyName, Throwable e) {
			log("Failed to run story " + storyName, e, MSG_WARN);
		}

		public void runningStory(String storyName) {
			log("Running story " + storyName, MSG_INFO);
		}

		public void storiesNotRun() {
			log("Stories not run", MSG_INFO);
		}

		public void generatingStoriesView(File outputDirectory,
				List<String> formats, Properties viewProperties) {
			log("Generating stories view in '" + outputDirectory
					+ "' using formats '" + formats + "'"
					+ " and view properties '" + viewProperties + "'",
					MSG_INFO);
		}

		public void storiesViewGenerationFailed(File outputDirectory,
				List<String> formats, Properties viewProperties,
				Throwable cause) {
			log("Failed to generate stories view in outputDirectory "
					+ outputDirectory + " using formats " + formats
					+ " and view properties '" + viewProperties + "'",
					MSG_WARN);
		}

		public void storiesViewGenerated(int scenarios, int failedScenarios) {
			log("Stories view generated with " + scenarios + " scenarios (of which  "
					+ failedScenarios + " failed)", MSG_INFO);
		}

		public void storiesViewNotGenerated() {
			log("Stories view not generated ", MSG_INFO);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}
	}

	// Setters used by Task to inject dependencies

	public void setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public void setTestSourceDirectory(String testSourceDirectory) {
		this.testSourceDirectory = testSourceDirectory;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setStoryClassNames(String classNamesCSV) {
		this.storyClassNames = asList(classNamesCSV.split(","));
	}

	public void setStoryIncludes(String includesCSV) {
		this.storyIncludes = asList(includesCSV.split(","));
	}

	public void setStoryExcludes(String excludesCSV) {
		this.storyExcludes = asList(excludesCSV.split(","));
	}

	public void setClassLoaderInjected(boolean classLoaderInjected) {
		this.classLoaderInjected = classLoaderInjected;
	}

	public void setBatch(boolean batch) {
		this.batch = batch;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public void setIgnoreFailureInStories(boolean ignoreFailureInStories) {
		this.ignoreFailureInStories = ignoreFailureInStories;
	}

	public void setIgnoreFailureInView(boolean ignoreFailureInView) {
		this.ignoreFailureInView = ignoreFailureInView;
	}

	public void setGenerateViewAfterStories(boolean generateViewAfterStories) {
		this.generateViewAfterStories = generateViewAfterStories;
	}

	public void setEmbedderClass(String embedderClass) {
		this.embedderClass = embedderClass;
	}
}
