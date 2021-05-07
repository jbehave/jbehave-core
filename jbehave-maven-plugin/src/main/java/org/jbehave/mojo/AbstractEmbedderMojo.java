package org.jbehave.mojo;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.MetaFilter;
import org.jbehave.core.embedder.NullEmbedderMonitor;
import org.jbehave.core.embedder.UnmodifiableEmbedderControls;
import org.jbehave.core.embedder.executors.ExecutorServiceFactory;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.ReportsCount;

/**
 * Abstract mojo that holds all the configuration parameters to specify and load
 * stories.
 */
public abstract class AbstractEmbedderMojo extends AbstractMojo {

    static final String TEST_SCOPE = "test";

    @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
    String sourceDirectory;

    @Parameter(defaultValue = "${project.build.testSourceDirectory}", required = true)
    String testSourceDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    String outputDirectory;

    @Parameter(defaultValue = "${project.build.testOutputDirectory}", required = true)
    String testOutputDirectory;

    /**
     * The scope of the mojo classpath, either "compile" or "test"
     */
    @Parameter(defaultValue = "compile")
    String scope;

    /**
     * Include filters, relative to the root source directory determined by the
     * scope
     */
    @Parameter
    List<String> includes;

    /**
     * Exclude filters, relative to the root source directory determined by the
     * scope
     */
    @Parameter
    List<String> excludes;

    /**
     * Compile classpath.
     */
    @Parameter(defaultValue = "${project.compileClasspathElements}", required = true, readonly = true)
    List<String> compileClasspathElements;

    /**
     * Test classpath.
     */
    @Parameter(defaultValue = "${project.testClasspathElements}", required = true, readonly = true)
    List<String> testClasspathElements;

    /**
     * The boolean flag to skip stories
     */
    @Parameter(defaultValue = "false")
    boolean skip = false;

    /**
     * The boolean flag to run in batch mode
     */
    @Parameter(defaultValue = "false")
    boolean batch = false;

    /**
     * The boolean flag to ignore failure in stories
     */
    @Parameter(defaultValue = "false")
    boolean ignoreFailureInStories = false;

    /**
     * The boolean flag to ignore failure in view
     */
    @Parameter(defaultValue = "false")
    boolean ignoreFailureInView = false;

    /**
     * The boolean flag to generate view after stories are run
     */
    @Parameter(defaultValue = "true")
    boolean generateViewAfterStories = true;

    /**
     * The boolean flag to output failures in verbose mode
     */
    @Parameter(defaultValue = "false")
    boolean verboseFailures = false;

    /**
     * The boolean flag to output filtering in verbose mode
     */
    @Parameter(defaultValue = "false")
    boolean verboseFiltering = false;

    /**
     * The story timeouts
     */
    @Parameter
    String storyTimeouts;

    /**
     * The boolean flag to fail on story timeout
     */
    @Parameter(defaultValue = "false")
    boolean failOnStoryTimeout = false;

    /**
     * The number of threads
     */
    @Parameter(defaultValue = "1")
    int threads = 1;

    /**
     * The embedder class
     */
    @Parameter(defaultValue = "org.jbehave.core.embedder.Embedder")
    String embedderClass = Embedder.class.getName();

    /**
     * The implementation class of the {@link ExecutorServiceFactory}
     */
    @Parameter
    String executorsClass;

    /**
     * The class that is injected with the embedder
     */
    @Parameter
    String injectableEmbedderClass;

    /**
     * The story finder used to retrieve story paths and class names
     */
    @Parameter
    String storyFinderClass = StoryFinder.class.getName();

    /**
     * The meta filters
     */
    @Parameter
    String[] metaFilters;

    /**
     * The system properties
     */
    @Parameter
    Properties systemProperties = new Properties();

    /**
     * The class loader
     */
    private EmbedderClassLoader classLoader;

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

    String outputDirectory() {
        if (isTestScope()) {
            return testOutputDirectory;
        }
        return outputDirectory;
    }

    URL codeLocation() {
        String outputDirectory = outputDirectory();
        try {
            return outputDirectory != null ? new File(outputDirectory).toURI().toURL() : null;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Failed to create code location from " + outputDirectory, e);
        }
    }

    /**
     * Returns the EmbedderClassLoader with the classpath element of the
     * selected scope.
     * 
     * @return An EmbedderClassLoader
     */
    protected EmbedderClassLoader classLoader() {
        if (classLoader == null) {
            classLoader = new EmbedderClassLoader(classpathElements());
        }
        return classLoader;
    }

    List<String> classpathElements() {
        List<String> classpathElements = compileClasspathElements;
        if (isTestScope()) {
            classpathElements = testClasspathElements;
        }
        return classpathElements;
    }

    /**
     * Finds story paths, using the {@link #newStoryFinder()}, in the
     * {@link #searchDirectory()} given specified {@link #includes} and
     * {@link #excludes}.
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
     * Finds class names, using the {@link #newStoryFinder()}, in the
     * {@link #searchDirectory()} given specified {@link #includes} and
     * {@link #excludes}.
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
        return classLoader().newInstance(StoryFinder.class, storyFinderClass);
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
        EmbedderClassLoader classLoader = classLoader();
        if (injectableEmbedderClass != null) {
            embedder = classLoader.newInstance(InjectableEmbedder.class, injectableEmbedderClass).injectedEmbedder();
        } else {
            embedder = classLoader.newInstance(Embedder.class, embedderClass);
        }
        
        URL codeLocation = codeLocation();
        if (codeLocation != null) {
            embedder.configuration().storyReporterBuilder().withCodeLocation(codeLocation);
        }

        embedder.useClassLoader(classLoader);
        embedder.useEmbedderControls(embedderControls());
        if (executorsClass != null) {
            ExecutorServiceFactory executorServiceFactory = classLoader.newInstance(ExecutorServiceFactory.class, executorsClass);
            embedder.useExecutorService(executorServiceFactory.create(embedder.embedderControls()));
        }
        embedder.useEmbedderMonitor(embedderMonitor());
        if (isNotEmpty(metaFilters)) {
            List<String> filters = new ArrayList<>();
            for (String filter : metaFilters) {
                if (filter != null) {
                    filters.add(filter);
                }
            }
            embedder.useMetaFilters(filters);
        }
        if (!systemProperties.isEmpty()) {
            embedder.useSystemProperties(systemProperties);
        }
        return embedder;
    }

    protected EmbedderMonitor embedderMonitor() {
        return new MavenEmbedderMonitor();
    }

    protected EmbedderControls embedderControls() {
        EmbedderControls embedderControls = new EmbedderControls().doBatch(batch).doSkip(skip)
                .doGenerateViewAfterStories(generateViewAfterStories).doIgnoreFailureInStories(ignoreFailureInStories)
                .doIgnoreFailureInView(ignoreFailureInView).doVerboseFailures(verboseFailures)
                .doVerboseFiltering(verboseFiltering)
                .doFailOnStoryTimeout(failOnStoryTimeout).useThreads(threads);
        if (storyTimeouts != null) {
            embedderControls.useStoryTimeouts(storyTimeouts);
        }        
        return new UnmodifiableEmbedderControls(embedderControls);
    }

    protected class MavenEmbedderMonitor extends NullEmbedderMonitor {

        @Override
        public void batchFailed(BatchFailures failures) {
            getLog().warn("Failed to run batch " + failures);
        }

        @Override
        public void beforeOrAfterStoriesFailed() {
            getLog().warn("Failed to run before or after stories steps");
        }

        @Override
        public void embeddableFailed(String name, Throwable cause) {
            getLog().warn("Failed to run embeddable " + name, cause);
        }

        @Override
        public void embeddableNotConfigurable(String name) {
            getLog().warn("Embeddable " + name + " must be an instance of " + ConfigurableEmbedder.class);
        }

        @Override
        public void embeddablesSkipped(List<String> classNames) {
            getLog().info("Skipped embeddables " + classNames);
        }

        @Override
        public void metaExcluded(Meta meta, MetaFilter filter) {
            getLog().debug(meta + " excluded by filter '" + filter.asString() + "'");
        }

        @Override
        public void runningEmbeddable(String name) {
            getLog().info("Running embeddable " + name);
        }

        @Override
        public void runningStory(String path) {
            getLog().info("Running story " + path);
        }

        @Override
        public void storyFailed(String path, Throwable cause) {
            getLog().warn("Failed to run story " + path, cause);
        }

        @Override
        public void storiesSkipped(List<String> storyPaths) {
            getLog().info("Skipped stories " + storyPaths);
        }

        @Override
        public void storiesExcluded(List<Story> excluded, MetaFilter filter, boolean verbose) {
            StringBuilder sb = new StringBuilder();
            sb.append(excluded.size() + " stories excluded by filter: " + filter.asString() + "\n");
            if (verbose) {
                for (Story story : excluded) {
                    sb.append(story.getPath()).append("\n");
                }
            }
            getLog().info(sb.toString());
        }

        @Override
        public void scenarioExcluded(Scenario scenario, MetaFilter filter) {
            StringBuilder sb = new StringBuilder();
            sb.append("Scenario '"+scenario.getTitle()+"' excluded by filter: " + filter.asString() + "\n");
            getLog().info(sb.toString());
        }

        @Override
        public void runningWithAnnotatedEmbedderRunner(String className) {
            getLog().info("Running with AnnotatedEmbedderRunner '" + className + "'");
        }

        @Override
        public void annotatedInstanceNotOfType(Object annotatedInstance, Class<?> type) {
            getLog().warn("Annotated instance " + annotatedInstance + " not of type " + type);
        }

        @Override
        public void generatingReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
            getLog().info(
                    "Generating reports view to '" + outputDirectory + "' using formats '" + formats + "'"
                            + " and view properties '" + viewProperties + "'");
        }

        @Override
        public void reportsViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewProperties,
                Throwable cause) {
            String message = "Failed to generate reports view to '" + outputDirectory + "' using formats '" + formats
                    + "'" + " and view properties '" + viewProperties + "'";
            getLog().warn(message, cause);
        }

        @Override
        public void reportsViewGenerated(ReportsCount count) {
            getLog().info(
                    "Reports view generated with " + count.getStories() + " stories (of which "
                            + count.getStoriesPending() + " pending) containing " + count.getScenarios()
                            + " scenarios (of which " + count.getScenariosPending() + " pending)");
            if (count.getStoriesExcluded() > 0 || count.getScenariosExcluded() > 0) {
                getLog().info(
                        "Meta filters excluded " + count.getStoriesExcluded() + " stories and  "
                                + count.getScenariosExcluded() + " scenarios");
            }
        }

        @Override
        public void reportsViewFailures(ReportsCount count) {
            getLog().warn("Failures in reports view: " + count.getScenariosFailed() + " scenarios failed");
        }

        @Override
        public void reportsViewNotGenerated() {
            getLog().info("Reports view not generated");
        }

        @Override
        public void mappingStory(String storyPath, List<String> metaFilters) {
            getLog().info("Mapping story " + storyPath + " with meta filters " + metaFilters);
        }

        @Override
        public void generatingMapsView(File outputDirectory, StoryMaps storyMaps, Properties viewProperties) {
            getLog().info(
                    "Generating maps view to '" + outputDirectory + "' using story maps '" + storyMaps + "'"
                            + " and view properties '" + viewProperties + "'");
        }

        @Override
        public void mapsViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewProperties,
                Throwable cause) {
            getLog().warn(
                    "Failed to generate maps view to '" + outputDirectory + "' using story maps '" + storyMaps + "'"
                            + " and view properties '" + viewProperties + "'", cause);
        }

        @Override
        public void processingSystemProperties(Properties properties) {
            getLog().info("Processing system properties " + properties);
        }

        @Override
        public void systemPropertySet(String name, String value) {
            getLog().info("System property '" + name + "' set to '" + value + "'");
        }

        @Override
        public void storyTimeout(Story story, StoryDuration storyDuration) {
            getLog().warn(
                    "Story " + story.getPath() + " duration of " + storyDuration.getDurationInSecs()
                            + " seconds has exceeded timeout of " + storyDuration.getTimeoutInSecs() + " seconds");
        }

        @Override
        public void usingThreads(int threads) {
            getLog().info("Using " + threads + " threads");
        }

        @Override
        public void usingExecutorService(ExecutorService executorService) {
            getLog().info("Using executor service " + executorService);
        }

        @Override
        public void usingControls(EmbedderControls embedderControls) {
            getLog().info("Using controls " + embedderControls);
        }
        
        @Override
        public void invalidTimeoutFormat(String path) {
            getLog().warn("Failed to set specific story timeout for story " + path + " because 'storyTimeoutInSecsByPath' has incorrect format");
            getLog().warn("'storyTimeoutInSecsByPath' must be a CSV of regex expressions matching story paths. E.g. \"*/long/*.story:5000,*/short/*.story:200\"");
        }

        @Override
        public void usingTimeout(String path, long timeout) {
            getLog().info("Using timeout for story " + path + " of "+timeout + " secs.");
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }

    }
}
