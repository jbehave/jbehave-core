package org.jbehave.mojo;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.jbehave.core.RunnableStory;
import org.jbehave.core.StoryClassLoader;
import org.jbehave.core.StoryEmbedder;
import org.jbehave.core.parser.StoryPathFinder;
import org.jbehave.core.runner.StoryRunnerMode;
import org.jbehave.core.runner.StoryRunnerMonitor;

/**
 * Abstract mojo that holds all the configuration parameters to specify and load
 * stories.
 * 
 * @author Mauro Talevi
 */
public abstract class AbstractStoryMojo extends AbstractMojo {

    private static final String TEST_SCOPE = "test";

    /**
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     * @readonly
     */
    private String sourceDirectory;

    /**
     * @parameter expression="${project.build.testSourceDirectory}"
     * @required
     * @readonly
     */
    private String testSourceDirectory;

    /**
     * The scope of the mojo classpath, either "compile" or "test"
     * 
     * @parameter default-value="compile"
     */
    private String scope;

    /**
     * Story class names, if specified take precedence over the names
     * specified via the "storyIncludes" and "storyExcludes" parameters
     * 
     * @parameter
     */
    private List<String> storyClassNames;

    /**
     * Story include filters, relative to the root source directory
     * determined by the scope
     * 
     * @parameter
     */
    private List<String> storyIncludes;

    /**
     * Story exclude filters, relative to the root source directory
     * determined by the scope
     * 
     * @parameter
     */
    private List<String> storyExcludes;

    /**
     * Compile classpath.
     * 
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> compileClasspathElements;

    /**
     * Test classpath.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> testClasspathElements;

    /**
     * The boolean flag to determined if class loader is injected in story class
     * 
     * @parameter default-value="false"
     */
    private boolean classLoaderInjected;
    
    /**
     * The boolean flag to skip stories
     * 
     * @parameter default-value="false"
     */
    private boolean skip;
    
    /**
     * The boolean flag to ignore failure in stories
     * 
     * @parameter default-value="false"
     */
    private boolean ignoreFailureInStories;
    
    /**
     * The boolean flag to ignore failure in reports
     * 
     * @parameter default-value="false"
     */
    private boolean ignoreFailureInReports;

    /**
     * The boolean flag to run in batch mode
     *
     * @parameter default-value="false"
     */
    private boolean batch;

    /**
     * The boolean flag to determined if reports are rendered after stories are run
     * 
     * @parameter default-value="true"
     */
    private boolean renderReportsAfterStories;
    
    /**
     * The story embedder to run the stories
     *
     * @parameter default-value="org.jbehave.core.StoryEmbedder"
     */
    private String storyEmbedder;
    
    /**
     * Used to find story class names
     */
    private StoryPathFinder finder = new StoryPathFinder();

    /**
     * Determines if the scope of the mojo classpath is "test"
     * 
     * @return A boolean <code>true</code> if test scoped
     */
    private boolean isTestScope() {
        return TEST_SCOPE.equals(scope);
    }

    private String rootSourceDirectory() {
        if (isTestScope()) {
            return testSourceDirectory;
        }
        return sourceDirectory;
    }

    private List<String> findStoryClassNames() {
        getLog().debug("Searching for story class names including "+storyIncludes+" and excluding "+storyExcludes);
        List<String> storyClassNames = finder.listStoryPaths(rootSourceDirectory(), null, storyIncludes,
                storyExcludes);
        getLog().debug("Found story class names: " + storyClassNames);
        return storyClassNames;
    }

    /**
     * Creates the StoryClassLoader with the classpath element of the
     * selected scope
     * 
     * @return A StoryClassLoader
     * @throws MalformedURLException
     */
    protected StoryClassLoader createStoryClassLoader() throws MalformedURLException {
        return new StoryClassLoader(classpathElements());
    }

    private List<String> classpathElements() {
        List<String> classpathElements = compileClasspathElements;
        if (isTestScope()) {
            classpathElements = testClasspathElements;
        }
        return classpathElements;
    }
    
    protected List<String> storyPaths() {
        getLog().debug("Searching for story paths including "+ storyIncludes +" and excluding "+ storyExcludes);
        List<String> storyPaths = finder.listStoryPaths(rootSourceDirectory(), null, storyIncludes,
                storyExcludes);
        getLog().info("Found story paths: " + storyPaths);
        return storyPaths;
    }
    
    /**
     * Returns the list of runnable stories, whose class names are either
     * specified via the parameter "storyClassNames" (which takes precedence)
     * or found using the parameters "storyIncludes" and "storyExcludes".
     * 
     * @return A List of RunnableStory
     * @throws MojoExecutionException
     */
    protected List<RunnableStory> stories() throws MojoExecutionException {
        List<String> names = storyClassNames;
        if (names == null || names.isEmpty()) {
            names = findStoryClassNames();
        }
        if (names.isEmpty()) {
            getLog().info("No stories to run.");
        }
        StoryClassLoader classLoader = null;
        try {
            classLoader = createStoryClassLoader();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create story class loader", e);
        }
        List<RunnableStory> stories = new ArrayList<RunnableStory>();
        for (String name : names) {
            try {
                if (!isStoryAbstract(classLoader, name)) {
                    stories.add(storyFor(classLoader, name));
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Failed to instantiate story '" + name + "'", e);
            }
        }
        return stories;
    }

    private boolean isStoryAbstract(StoryClassLoader classLoader, String name) throws ClassNotFoundException {
        return Modifier.isAbstract(classLoader.loadClass(name).getModifiers());
    }

    private RunnableStory storyFor(StoryClassLoader classLoader, String name) {
        if ( classLoaderInjected ){
            try {
                return classLoader.newStory(name, ClassLoader.class);
            } catch (RuntimeException e) {
                throw new RuntimeException("JBehave is trying to instantiate your RunnableStory class '" 
                        + name + "' with a ClassLoader as a parameter.  " +
                        "If this is wrong, change the Maven configuration for the plugin to include " +
                        "<classLoaderInjected>false</classLoaderInjected>" , e);
            }
        }
        return classLoader.newStory(name);
    }

    protected StoryEmbedder newStoryEmbedder() {
        try {
             StoryEmbedder embedder = (StoryEmbedder) createStoryClassLoader().loadClass(storyEmbedder).newInstance();
             embedder.useRunnerMonitor(runnerMonitor());
             embedder.useRunnerMode(runnerMode());
             return embedder;
        } catch ( Exception e) {
            throw new RuntimeException("Failed to create story embedder "+storyEmbedder, e);
        }
    }

    protected MavenRunnerMonitor runnerMonitor() {
		return new MavenRunnerMonitor();
	}

	protected StoryRunnerMode runnerMode() {
		return new StoryRunnerMode(batch, skip, ignoreFailureInStories, ignoreFailureInReports, renderReportsAfterStories);
	}

	protected class MavenRunnerMonitor implements StoryRunnerMonitor {
        public void storiesBatchFailed(String failedStories) {
            getLog().warn("Failed to run stories batch: "+failedStories);
        }

        public void storyFailed(String storyName, Throwable e) {
            getLog().warn("Failed to run story "+storyName, e);
        }

        public void runningStory(String storyName) {
            getLog().info("Running story "+storyName);
        }

        public void storiesNotRun() {
            getLog().info("Stories not run");
        }

		public void renderingReports(File outputDirectory,
				List<String> formats, Properties templateProperties) {
            getLog().info("Rendering reports in '" + outputDirectory + "' using formats '" + formats + "'" 
        		    + " and template properties '"+templateProperties+"'");
		}

		public void reportRenderingFailed(File outputDirectory,
				List<String> formats, Properties templateProperties, Throwable cause) {
            String message = "Failed to render reports in outputDirectory " + outputDirectory
            		+ " using formats " + formats + " and template properties '"+templateProperties+"'";
            getLog().warn(message, cause);
		}
		
		public void reportsRendered(int scenarios, int failedScenarios) {
			getLog().info("Reports rendered with " + scenarios
            		+ " scenarios (of which  " + failedScenarios + " failed)");
		}
		
		public void reportsNotRendered() {
			getLog().info("Reports not rendered");
		}
		
		@Override
		public String toString() {
			return this.getClass().getSimpleName();
		}

    }
}
