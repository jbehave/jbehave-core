package org.jbehave.mojo;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.MetaFilter;
import org.jbehave.core.embedder.UnmodifiableEmbedderControls;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.model.Meta;

/**
 * Abstract mojo that holds all the configuration parameters to specify and load
 * stories.
 * 
 * @requiresDependencyResolution
 */
public abstract class AbstractEmbedderMojo extends AbstractMojo {

    static final String TEST_SCOPE = "test";

    /**
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     * @readonly
     */
    String sourceDirectory;

    /**
     * @parameter expression="${project.build.testSourceDirectory}"
     * @required
     * @readonly
     */
    String testSourceDirectory;

    /**
     * The scope of the mojo classpath, either "compile" or "test"
     * 
     * @parameter default-value="compile"
     */
    String scope;

    /**
     * Include filters, relative to the root source directory determined by the
     * scope
     * 
     * @parameter
     */
    List<String> includes;

    /**
     * Exclude filters, relative to the root source directory determined by the
     * scope
     * 
     * @parameter
     */
    List<String> excludes;

    /**
     * Compile classpath.
     * 
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
     List<String> compileClasspathElements;

    /**
     * Test classpath.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
     List<String> testClasspathElements;

    /**
     * The boolean flag to skip stories
     * 
     * @parameter default-value="false"
     */
    boolean skip = false;

    /**
     * The boolean flag to run in batch mode
     * 
     * @parameter default-value="false"
     */
    boolean batch = false;

    /**
     * The boolean flag to ignore failure in stories
     * 
     * @parameter default-value="false"
     */
    boolean ignoreFailureInStories = false;

    /**
     * The boolean flag to ignore failure in view
     * 
     * @parameter default-value="false"
     */
    boolean ignoreFailureInView = false;

    /**
     * The boolean flag to generate view after stories are run
     * 
     * @parameter default-value="true"
     */
    boolean generateViewAfterStories = true;

    /**
     * The embedder class
     * 
     * @parameter default-value="org.jbehave.core.embedder.Embedder"
     */
    String embedderClass = Embedder.class.getName();

    /**
     * The class that is injected with the embedder
     * 
     * @parameter
     */
    String injectableEmbedderClass;

    /**
     * The annotated embedder runner class
     * 
     * @parameter default-value="org.jbehave.core.junit.AnnotatedEmbedderRunner"
     */
    protected String annotatedEmbedderRunnerClass = AnnotatedEmbedderRunner.class.getName();
    
    /**
     * Used to find story paths and class names
     * 
     * @parameter
     */
    String storyFinderClass = StoryFinder.class.getName();

    /**
     * The meta filter
     * 
     * @parameter default-value=""
     */
    String metaFilter = "";
    
    /**
     * Determines if the scope of the mojo classpath is "test"
     * 
     * @return A boolean <code>true</code> if test scoped
     */
    boolean isTestScope() {
        return TEST_SCOPE.equals(scope);
    }

    String searchDirectory() {
        if (isTestScope()) {
            return testSourceDirectory;
        }
        return sourceDirectory;
    }

    /**
     * Creates the EmbedderClassLoader with the classpath element of the
     * selected scope
     * 
     * @return A EmbedderClassLoader
     */
    protected EmbedderClassLoader createClassLoader() {
        return new EmbedderClassLoader(classpathElements());
    }

    List<String> classpathElements() {
        List<String> classpathElements = compileClasspathElements;
        if (isTestScope()) {
            classpathElements = testClasspathElements;
        }
        return classpathElements;
    }

    /**
     * Finds story paths, using the {@link #newStoryFinder()}, in the {@link #searchDirectory()} given
     * specified {@link #includes} and {@link #excludes}.
     * 
     * @return A List of story paths found
     */
    protected List<String> storyPaths() {
        getLog().debug("Searching for story paths including " + includes + " and excluding " + excludes);
        List<String> storyPaths = newStoryFinder().findPaths(searchDirectory(), includes, excludes);
        getLog().info("Found story paths: " + storyPaths);
        return storyPaths;
    }

    /**
     * Finds class names, using the {@link #newStoryFinder()}, in the {@link #searchDirectory()} given
     * specified {@link #includes} and {@link #excludes}.
     * 
     * @return A List of class names found
     */
    protected List<String> classNames() {
        getLog().debug("Searching for class names including " + includes + " and excluding " + excludes);
        List<String> classNames = newStoryFinder().findClassNames(searchDirectory(), includes, excludes);
        getLog().info("Found class names: " + classNames);
        return classNames;
    }

    /**
     * Creates an instance of StoryFinder, using the {@link #storyFinderClass}
     * 
     * @return A StoryFinder
     */
    protected StoryFinder newStoryFinder() {
        return createClassLoader().newInstance(StoryFinder.class, storyFinderClass);
    }
    
    /**
     * Creates an instance of Embedder, either using
     * {@link #injectableEmbedderClass} (if set) or defaulting to
     * {@link #embedderClass}.
     * 
     * @return An Embedder
     */
    protected Embedder newEmbedder() {
        Embedder embedder = null;
        EmbedderClassLoader classLoader = createClassLoader();
        if (injectableEmbedderClass != null) {
            embedder = classLoader.newInstance(InjectableEmbedder.class, injectableEmbedderClass).injectedEmbedder();
        } else {
            embedder = classLoader.newInstance(Embedder.class, embedderClass);
        }
        EmbedderMonitor embedderMonitor = embedderMonitor();
        embedder.useEmbedderMonitor(embedderMonitor);
        embedder.useMetaFilter(new MetaFilter(metaFilter, embedderMonitor));
        embedder.useEmbedderControls(embedderControls());
        return embedder;
    }

    protected EmbedderMonitor embedderMonitor() {
        return new MavenEmbedderMonitor();
    }

    protected EmbedderControls embedderControls() {
        return new UnmodifiableEmbedderControls(new EmbedderControls().doBatch(batch).doSkip(skip)
                .doGenerateViewAfterStories(generateViewAfterStories).doIgnoreFailureInStories(ignoreFailureInStories)
                .doIgnoreFailureInView(ignoreFailureInView));
    }

    protected class MavenEmbedderMonitor implements EmbedderMonitor {

        public void batchFailed(BatchFailures failures) {
            getLog().warn("Failed to run batch " + failures);            
        }

        public void embeddableFailed(String name, Throwable cause) {
            getLog().warn("Failed to run embeddable " + name, cause);            
        }

        public void embeddablesSkipped(List<String> classNames) {
            getLog().info("Skipped embeddables " + classNames);            
        }

        public void metaNotAllowed(Meta meta, MetaFilter filter) {
            getLog().info(meta +" not allowed by "+filter.asString());        
        }
        
        public void runningEmbeddable(String name) {
            getLog().info("Running embeddable " + name);
        }

        public void runningStory(String path) {
            getLog().info("Running story " + path);
        }

        public void storiesSkipped(List<String> storyPaths) {
            getLog().info("Skipped stories " + storyPaths);            
        }

        public void storyFailed(String path, Throwable cause) {
            getLog().warn("Failed to run story " + path, cause);
        }

        public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
            getLog().warn("Annotated instance " + annotatedInstance + " not of type " + type);
        }

        public void generatingStoriesView(File outputDirectory, List<String> formats, Properties viewProperties) {
            getLog().info(
                    "Generating stories view in '" + outputDirectory + "' using formats '" + formats + "'"
                            + " and view properties '" + viewProperties + "'");
        }

        public void storiesViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
                Throwable cause) {
            String message = "Failed to generate stories view in outputDirectory " + outputDirectory
                    + " using formats " + formats + " and view properties '" + viewProperties + "'";
            getLog().warn(message, cause);
        }

        public void storiesViewGenerated(int stories, int scenarios, int failedScenarios) {
            getLog().info(
                    "Stories view generated with " + stories + " stories containing " + scenarios
                            + " scenarios (of which  " + failedScenarios + " failed)");
        }

        public void storiesViewNotGenerated() {
            getLog().info("Stories view not generated");
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }

    }
}
