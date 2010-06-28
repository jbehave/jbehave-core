package org.jbehave.ant;

import static java.util.Arrays.asList;
import static org.apache.tools.ant.Project.MSG_DEBUG;
import static org.apache.tools.ant.Project.MSG_INFO;
import static org.apache.tools.ant.Project.MSG_WARN;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jbehave.core.Embeddable;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.embedder.UnmodifiableEmbedderControls;
import org.jbehave.core.io.StoryFinder;

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
     * Used to find stories
     */
    private StoryFinder finder = new StoryFinder();

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

    /**
     * Creates the EmbedderClassLoader with the classpath element of the selected
     * scope
     * 
     * @return A EmbedderClassLoader
     * @throws BuildException
     */
    protected EmbedderClassLoader createClassLoader() {
        try {
            return new EmbedderClassLoader(asList(new String[] {}));
        } catch (MalformedURLException e) {
            throw new BuildException("Failed to create "+EmbedderClassLoader.class, e);
        }
    }

    protected EmbedderMonitor embedderMonitor() {
        return new AntEmbedderMonitor();
    }

    protected EmbedderControls embedderControls() {
        return new UnmodifiableEmbedderControls(new EmbedderControls().doBatch(batch).doSkip(skip)
                .doGenerateViewAfterStories(generateViewAfterStories).doIgnoreFailureInStories(ignoreFailureInStories)
                .doIgnoreFailureInView(ignoreFailureInView));
    }

    protected List<String> storyPaths() {
        log("Searching for story paths including " + storyIncludes + " and excluding " + storyExcludes, MSG_DEBUG);
        List<String> storyPaths = finder.findPaths(rootSourceDirectory(), storyIncludes, storyExcludes);
        log("Found story paths: " + storyPaths, MSG_INFO);
        return storyPaths;
    }

    protected List<Embeddable> embeddables() throws BuildException {
        log("Searching for embeddables including " + storyIncludes + " and excluding " + storyExcludes, MSG_DEBUG);
        List<Embeddable> stories = finder
                .findEmbeddables(rootSourceDirectory(), storyIncludes, storyExcludes, createClassLoader());
        log("Found embeddables: " + stories, MSG_INFO);
        return stories;
    }

    protected Embedder newEmbedder() {
        try {
            Embedder embedder = createClassLoader().newInstance(Embedder.class, embedderClass);
            embedder.useEmbedderMonitor(embedderMonitor());
            embedder.useEmbedderControls(embedderControls());
            return embedder;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create embedder " + embedderClass, e);
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

        public void generatingStoriesView(File outputDirectory, List<String> formats, Properties viewProperties) {
            log("Generating stories view in '" + outputDirectory + "' using formats '" + formats + "'"
                    + " and view properties '" + viewProperties + "'", MSG_INFO);
        }

        public void storiesViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
                Throwable cause) {
            log("Failed to generate stories view in outputDirectory " + outputDirectory + " using formats " + formats
                    + " and view properties '" + viewProperties + "'", MSG_WARN);
        }

        public void storiesViewGenerated(int scenarios, int failedScenarios) {
            log("Stories view generated with " + scenarios + " scenarios (of which  " + failedScenarios + " failed)",
                    MSG_INFO);
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

    public void setStoryIncludes(String includesCSV) {
        this.storyIncludes = asList(includesCSV.split(","));
    }

    public void setStoryExcludes(String excludesCSV) {
        this.storyExcludes = asList(excludesCSV.split(","));
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
