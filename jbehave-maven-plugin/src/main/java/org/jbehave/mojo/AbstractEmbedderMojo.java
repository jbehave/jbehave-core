package org.jbehave.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.UnmodifiableEmbedderControls;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;

/**
 * Abstract mojo that holds all the configuration parameters to specify and load
 * stories.
 * 
 * @author Mauro Talevi
 */
public abstract class AbstractEmbedderMojo extends AbstractMojo {

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
     * Include filters, relative to the root source directory determined
     * by the scope
     * 
     * @parameter
     */
    private List<String> includes;

    /**
     * Exclude filters, relative to the root source directory determined
     * by the scope
     * 
     * @parameter
     */
    private List<String> excludes;

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
     * The boolean flag to skip stories
     * 
     * @parameter default-value="false"
     */
    private boolean skip;

    /**
     * The boolean flag to run in batch mode
     * 
     * @parameter default-value="false"
     */
    private boolean batch;

    /**
     * The boolean flag to ignore failure in stories
     * 
     * @parameter default-value="false"
     */
    private boolean ignoreFailureInStories;

    /**
     * The boolean flag to ignore failure in view
     * 
     * @parameter default-value="false"
     */
    private boolean ignoreFailureInView;

    /**
     * The boolean flag to generate view after stories are run
     * 
     * @parameter default-value="true"
     */
    private boolean generateViewAfterStories;

    /**
     * The embedder class 
     * 
     * @parameter default-value="org.jbehave.core.embedder.Embedder"
     */
    private String embedderClass;

    /**
     * The class that is injected with the embedder
     * 
     * @parameter
     */
    private String injectableEmbedderClass;

    /**
     * The annotated embedder runner class 
     * 
     * @parameter default-value="org.jbehave.core.junit.AnnotatedEmbedderRunner"
     */
    private String annotatedEmbedderRunnerClass;

    /**
     * Used to find story paths and embeddables
     */
    private StoryFinder finder = new StoryFinder();

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

    /**
     * Creates the EmbedderClassLoader with the classpath element of the
     * selected scope
     * 
     * @return A EmbedderClassLoader
     */
    protected EmbedderClassLoader createClassLoader() {
        try {
            return new EmbedderClassLoader(classpathElements());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create " + EmbedderClassLoader.class, e);
        }
    }

    private List<String> classpathElements() {
        List<String> classpathElements = compileClasspathElements;
        if (isTestScope()) {
            classpathElements = testClasspathElements;
        }
        return classpathElements;
    }

    protected List<String> storyPaths() {
        getLog().debug("Searching for story paths including " + includes + " and excluding " + excludes);
        List<String> storyPaths = finder.findPaths(rootSourceDirectory(), includes, excludes);
        getLog().info("Found story paths: " + storyPaths);
        return storyPaths;
    }

    protected List<String> classNames() {
        getLog().debug("Searching for class names including " + includes + " and excluding " + excludes);
        List<String> classNames = finder.findClassNames(rootSourceDirectory(), includes, excludes);
        getLog().info("Found class names: " + classNames);
        return classNames;
    }
    
    /**
     * Creates an instance of Embedder, either using {@link #injectableEmbedderClass} (if set)
     * or defaulting to {@link #embedderClass}.
     * 
     * @return An Embedder
     */
    protected Embedder newEmbedder() {
        Embedder embedder = null;
        EmbedderClassLoader classLoader = createClassLoader();
        if ( injectableEmbedderClass != null ){
            embedder = classLoader.newInstance(InjectableEmbedder.class, injectableEmbedderClass).injectedEmbedder();
        } else {
            embedder = classLoader.newInstance(Embedder.class, embedderClass);
        }
        embedder.useEmbedderMonitor(embedderMonitor());
        embedder.useEmbedderControls(embedderControls());
        return embedder;
    }

    protected List<AnnotatedEmbedderRunner> annotatedEmbedderRunners() {        
        getLog().debug("Searching for annotated classes including " + includes + " and excluding " + excludes);
        EmbedderClassLoader classLoader = createClassLoader();
        List<Class<?>> classes = finder.findClasses(rootSourceDirectory(), includes, excludes, classLoader);
        Class<? extends AnnotatedEmbedderRunner> runnerClass = annotatedEmbedderRunnerClass(classLoader);
        getLog().info("Creating runner " + runnerClass + " for " + classes);
        List<AnnotatedEmbedderRunner> runners = new ArrayList<AnnotatedEmbedderRunner>();
        for (Class<?> annotatedClass : classes) {
            runners.add(newAnnotatedEmbedder(runnerClass, annotatedClass));
        }
        return runners;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends AnnotatedEmbedderRunner> annotatedEmbedderRunnerClass(EmbedderClassLoader classLoader) {
        try {
            return (Class<? extends AnnotatedEmbedderRunner>) classLoader.loadClass(annotatedEmbedderRunnerClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private AnnotatedEmbedderRunner newAnnotatedEmbedder(Class<? extends AnnotatedEmbedderRunner> annotatedEmbedderClass, Class<?> annotatedClass) {
        try {
            return (AnnotatedEmbedderRunner) annotatedEmbedderClass.getConstructor(Class.class).newInstance(annotatedClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        public void storiesBatchFailed(String failedStories) {
            getLog().warn("Failed to run stories batch: " + failedStories);
        }

        public void storyFailed(String storyName, Throwable e) {
            getLog().warn("Failed to run story " + storyName, e);
        }

        public void runningStory(String storyName) {
            getLog().info("Running story " + storyName);
        }

        public void storiesNotRun() {
            getLog().info("Stories not run");
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

        public void storiesViewGenerated(int scenarios, int failedScenarios) {
            getLog().info(
                    "Stories view generated with " + scenarios + " scenarios (of which  " + failedScenarios
                            + " failed)");
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
