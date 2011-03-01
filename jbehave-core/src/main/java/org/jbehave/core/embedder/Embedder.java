package org.jbehave.core.embedder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.ReportsCount;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Stepdoc;

/**
 * Represents an entry point to all of JBehave's functionality that is
 * embeddable into other launchers, such as IDEs or CLIs.
 */
public class Embedder {

    private Configuration configuration = new MostUsefulConfiguration();
    private List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
    private EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
    private EmbedderControls embedderControls = new EmbedderControls();
    private List<String> metaFilters = Arrays.asList();
    private Properties systemProperties = new Properties();
    private StoryMapper storyMapper;
    private StoryRunner storyRunner;
    private EmbedderMonitor embedderMonitor;

    public Embedder() {
        this(new StoryMapper(), new StoryRunner(), new PrintStreamEmbedderMonitor());
    }

    public Embedder(StoryMapper storyMapper, StoryRunner storyRunner, EmbedderMonitor embedderMonitor) {
        this.storyMapper = storyMapper;
        this.storyRunner = storyRunner;
        this.embedderMonitor = embedderMonitor;
    }

    public void mapStoriesAsPaths(List<String> storyPaths) {
        EmbedderControls embedderControls = embedderControls();
        if (embedderControls.skip()) {
            embedderMonitor.storiesSkipped(storyPaths);
            return;
        }
        
        processSystemProperties();        

        for (String storyPath : storyPaths) {
            Story story = storyRunner.storyOfPath(configuration, storyPath);
            embedderMonitor.mappingStory(storyPath, metaFilters);
            storyMapper.map(story, new MetaFilter(""));
            for (String filter : metaFilters) {
                storyMapper.map(story, new MetaFilter(filter));
            }
        }

        generateMapsView(storyMapper.getStoryMaps());

    }

    private void generateMapsView(StoryMaps storyMaps) {
        StoryReporterBuilder builder = configuration().storyReporterBuilder();
        File outputDirectory = builder.outputDirectory();
        Properties viewResources = builder.viewResources();
        ViewGenerator viewGenerator = configuration().viewGenerator();
        try {
            embedderMonitor.generatingMapsView(outputDirectory, storyMaps, viewResources);
            viewGenerator.generateMapsView(outputDirectory, storyMaps, viewResources);
        } catch (RuntimeException e) {
            embedderMonitor.mapsViewGenerationFailed(outputDirectory, storyMaps, viewResources, e);
            throw new ViewGenerationFailed(outputDirectory, storyMaps, viewResources, e);
        }
    }

    public void runAsEmbeddables(List<String> classNames) {
        EmbedderControls embedderControls = embedderControls();
        if (embedderControls.skip()) {
            embedderMonitor.embeddablesSkipped(classNames);
            return;
        }

        BatchFailures batchFailures = new BatchFailures();
        for (Embeddable embeddable : embeddables(classNames, classLoader())) {
            String name = embeddable.getClass().getName();
            try {
                embedderMonitor.runningEmbeddable(name);
                embeddable.useEmbedder(this);
                embeddable.run();
            } catch (Throwable e) {
                if (embedderControls.batch()) {
                    // collect and postpone decision to throw exception
                    batchFailures.put(name, e);
                } else {
                    if (embedderControls.ignoreFailureInStories()) {
                        embedderMonitor.embeddableFailed(name, e);
                    } else {
                        throw new RunningEmbeddablesFailed(name, e);
                    }
                }
            }
        }

        if (embedderControls.batch() && batchFailures.size() > 0) {
            if (embedderControls.ignoreFailureInStories()) {
                embedderMonitor.batchFailed(batchFailures);
            } else {
                throw new RunningEmbeddablesFailed(batchFailures);
            }
        }

    }

    private List<Embeddable> embeddables(List<String> classNames, EmbedderClassLoader classLoader) {
        List<Embeddable> embeddables = new ArrayList<Embeddable>();
        for (String className : classNames) {
            if (!classLoader.isAbstract(className)) {
                embeddables.add(classLoader.newInstance(Embeddable.class, className));
            }
        }
        return embeddables;
    }

    public void runStoriesWithAnnotatedEmbedderRunner(String runnerClass, List<String> classNames) {
        List<AnnotatedEmbedderRunner> runners = annotatedEmbedderRunners(runnerClass, classNames, classLoader());
        for (AnnotatedEmbedderRunner runner : runners) {
            try {
                Object annotatedInstance = runner.createTest();
                if (annotatedInstance instanceof Embeddable) {
                    ((Embeddable) annotatedInstance).run();
                } else {
                    embedderMonitor.annotatedInstanceNotOfType(annotatedInstance, Embeddable.class);
                }
            } catch (Throwable e) {
                throw new AnnotatedEmbedderRunFailed(runner, e);
            }
        }
    }

    private List<AnnotatedEmbedderRunner> annotatedEmbedderRunners(String runnerClassName, List<String> classNames,
            EmbedderClassLoader classLoader) {
        Class<?> runnerClass = loadClass(runnerClassName, classLoader);
        List<AnnotatedEmbedderRunner> runners = new ArrayList<AnnotatedEmbedderRunner>();
        for (String annotatedClassName : classNames) {
            runners.add(newAnnotatedEmbedderRunner(runnerClass, annotatedClassName, classLoader));
        }
        return runners;
    }

    private AnnotatedEmbedderRunner newAnnotatedEmbedderRunner(Class<?> runnerClass, String annotatedClassName,
            EmbedderClassLoader classLoader) {
        try {
            Class<?> annotatedClass = loadClass(annotatedClassName, classLoader);
            return (AnnotatedEmbedderRunner) runnerClass.getConstructor(Class.class).newInstance(annotatedClass);
        } catch (Exception e) {
            throw new AnnotatedEmbedderRunnerInstantiationFailed(runnerClass, annotatedClassName, classLoader, e);
        }
    }

    private Class<?> loadClass(String className, EmbedderClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ClassLoadingFailed(className, classLoader, e);
        }
    }

    public void runStoriesAsPaths(List<String> storyPaths) {

        ExecutorService executorService = createExecutorService();

        processSystemProperties();

        final EmbedderControls embedderControls = embedderControls();
        if (embedderControls.skip()) {
            embedderMonitor.storiesSkipped(storyPaths);
            return;
        }

        final Configuration configuration = configuration();
        final List<CandidateSteps> candidateSteps = candidateSteps();

        storyRunner.runBeforeOrAfterStories(configuration, candidateSteps, Stage.BEFORE);

        final BatchFailures batchFailures = new BatchFailures();
        buildReporters(configuration, storyPaths);
        final MetaFilter filter = new MetaFilter(StringUtils.join(metaFilters, " "), embedderMonitor);

        List<Future<Throwable>> futures = new ArrayList<Future<Throwable>>();

        for (final String storyPath : storyPaths) {
            futures.add(executorService.submit(new Callable<Throwable>() {
                public Throwable call() throws Exception {
                    try {
                        embedderMonitor.runningStory(storyPath);
                        Story story = storyRunner.storyOfPath(configuration, storyPath);
                        storyRunner.run(configuration, candidateSteps, story, filter);
                    } catch (Throwable e) {
                        if (embedderControls.batch()) {
                            // collect and postpone decision to throw exception
                            batchFailures.put(storyPath, e);
                        } else {
                            if (embedderControls.ignoreFailureInStories()) {
                                embedderMonitor.storyFailed(storyPath, e);
                            } else {
                                return new RunningStoriesFailed(storyPath, e);
                            }
                        }
                    }
                    return null;
                }
            }));
        }

        waitUntilAllDone(futures);

        checkForFailures(futures);

        storyRunner.runBeforeOrAfterStories(configuration, candidateSteps, Stage.AFTER);

        if (embedderControls.batch() && batchFailures.size() > 0) {
            if (embedderControls.ignoreFailureInStories()) {
                embedderMonitor.batchFailed(batchFailures);
            } else {
                throw new RunningStoriesFailed(batchFailures);
            }
        }

        if (embedderControls.generateViewAfterStories()) {
            generateReportsView();
        }
    }

    /**
     * Creates a {@link ThreadPoolExecutor} using the number of threads defined
     * in the {@link EmbedderControls#threads()}
     * 
     * @return An ExecutorService
     */
    protected ExecutorService createExecutorService() {
        int threads = embedderControls.threads();
        if (threads == 1) {
            // this is necessary for situations where people use the PerStoriesWebDriverSteps class.
            return new NonThreadingExecutorService();
        } else {
            return Executors.newFixedThreadPool(threads);
        }
    }

    private void waitUntilAllDone(List<Future<Throwable>> futures) {
        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (Future<Throwable> future : futures) {
                if (!future.isDone()) {
                    allDone = false;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    break;
                }
            }
        }
    }

    private void checkForFailures(List<Future<Throwable>> futures) {
        try {
            BatchFailures failures = new BatchFailures();
            for (Future<Throwable> future : futures) {
                Throwable failure = future.get();
                if (failure != null) {
                    failures.put(future.toString(), failure);
                }
            }
            if (failures.size() > 0) {
                throw new RunningStoriesFailed(failures);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildReporters(Configuration configuration, List<String> storyPaths) {
        StoryReporterBuilder reporterBuilder = configuration.storyReporterBuilder();
        reporterBuilder.withMultiThreading(embedderControls.threads() > 1);
        configuration.useStoryReporters(reporterBuilder.build(storyPaths));
    }

    public void generateReportsView() {
        StoryReporterBuilder builder = configuration().storyReporterBuilder();
        File outputDirectory = builder.outputDirectory();
        List<String> formatNames = builder.formatNames(true);
        generateReportsView(outputDirectory, formatNames, builder.viewResources());
    }

    public void generateReportsView(File outputDirectory, List<String> formats, Properties viewResources) {
        EmbedderControls embedderControls = embedderControls();

        if (embedderControls.skip()) {
            embedderMonitor.reportsViewNotGenerated();
            return;
        }
        ViewGenerator viewGenerator = configuration().viewGenerator();
        try {
            embedderMonitor.generatingReportsView(outputDirectory, formats, viewResources);
            viewGenerator.generateReportsView(outputDirectory, formats, viewResources);
        } catch (RuntimeException e) {
            embedderMonitor.reportsViewGenerationFailed(outputDirectory, formats, viewResources, e);
            throw new ViewGenerationFailed(outputDirectory, formats, viewResources, e);
        }
        ReportsCount count = viewGenerator.getReportsCount();
        embedderMonitor.reportsViewGenerated(count);
        if (!embedderControls.ignoreFailureInView() && count.getScenariosFailed() > 0) {
            throw new RunningStoriesFailed(count.getStories(), count.getScenarios(), count.getScenariosFailed());
        }

    }

    public void generateCrossReference() {
        StoryReporterBuilder builder = configuration().storyReporterBuilder();
        CrossReference crossReference = builder.crossReference();
        if (crossReference != null) {
            crossReference.outputToFiles(builder);
        }
    }

    public void reportStepdocs() {
        reportStepdocs(configuration(), candidateSteps());
    }

    public void reportStepdocsAsEmbeddables(List<String> classNames) {
        EmbedderControls embedderControls = embedderControls();
        if (embedderControls.skip()) {
            embedderMonitor.embeddablesSkipped(classNames);
            return;
        }

        for (Embeddable embeddable : embeddables(classNames, classLoader())) {
            if (embeddable instanceof ConfigurableEmbedder) {
                ConfigurableEmbedder configurableEmbedder = (ConfigurableEmbedder) embeddable;
                reportStepdocs(configurableEmbedder.configuration(), configurableEmbedder.candidateSteps());
            } else {
                embedderMonitor.embeddableNotConfigurable(embeddable.getClass().getName());
            }
        }
    }

    public void reportStepdocs(Configuration configuration, List<CandidateSteps> candidateSteps) {
        StepFinder finder = configuration.stepFinder();
        StepdocReporter reporter = configuration.stepdocReporter();
        List<Object> stepsInstances = finder.stepsInstances(candidateSteps);
        reporter.stepdocs(finder.stepdocs(candidateSteps), stepsInstances);
    }

    public void reportMatchingStepdocs(String stepAsString) {
        Configuration configuration = configuration();
        List<CandidateSteps> candidateSteps = candidateSteps();
        StepFinder finder = configuration.stepFinder();
        StepdocReporter reporter = configuration.stepdocReporter();
        List<Stepdoc> matching = finder.findMatching(stepAsString, candidateSteps);
        List<Object> stepsInstances = finder.stepsInstances(candidateSteps);
        reporter.stepdocsMatching(stepAsString, matching, stepsInstances);
    }

    public void processSystemProperties() {
        Properties properties = systemProperties();
        embedderMonitor.processingSystemProperties(properties);
        if (!properties.isEmpty()) {
            for (Object key : properties.keySet()) {
                String name = (String) key;
                String value = properties.getProperty(name);
                System.setProperty(name, value);
                embedderMonitor.systemPropertySet(name, value);
            }
        }
    }

    public EmbedderClassLoader classLoader() {
        return classLoader;
    }

    public Configuration configuration() {
        return configuration;
    }

    public List<CandidateSteps> candidateSteps() {
        return candidateSteps;
    }

    public EmbedderControls embedderControls() {
        return embedderControls;
    }

    public EmbedderMonitor embedderMonitor() {
        return embedderMonitor;
    }

    public List<String> metaFilters() {
        return metaFilters;
    }

    public StoryRunner storyRunner() {
        return storyRunner;
    }

    public Properties systemProperties() {
        return systemProperties;
    }

    public void useClassLoader(EmbedderClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void useConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void useCandidateSteps(List<CandidateSteps> candidateSteps) {
        this.candidateSteps = candidateSteps;
    }

    public void useEmbedderControls(EmbedderControls embedderControls) {
        this.embedderControls = embedderControls;
    }

    public void useEmbedderMonitor(EmbedderMonitor embedderMonitor) {
        this.embedderMonitor = embedderMonitor;
    }

    public void useMetaFilters(List<String> metaFilters) {
        this.metaFilters = metaFilters;
    }

    public void useStoryRunner(StoryRunner storyRunner) {
        this.storyRunner = storyRunner;
    }

    public void useSystemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @SuppressWarnings("serial")
    public static class ClassLoadingFailed extends RuntimeException {

        public ClassLoadingFailed(String className, EmbedderClassLoader classLoader, Throwable cause) {
            super("Failed to load class " + className + " with classLoader " + classLoader, cause);
        }

    }

    @SuppressWarnings("serial")
    public static class AnnotatedEmbedderRunnerInstantiationFailed extends RuntimeException {

        public AnnotatedEmbedderRunnerInstantiationFailed(Class<?> runnerClass, String annotatedClassName,
                EmbedderClassLoader classLoader, Throwable cause) {
            super("Failed to instantiate annotated embedder runner " + runnerClass + " with annotatedClassName "
                    + annotatedClassName + " and classLoader " + classLoader, cause);
        }

    }

    @SuppressWarnings("serial")
    public static class AnnotatedEmbedderRunFailed extends RuntimeException {

        public AnnotatedEmbedderRunFailed(AnnotatedEmbedderRunner runner, Throwable cause) {
            super("Annotated embedder run failed with runner " + runner.toString(), cause);
        }

    }

    @SuppressWarnings("serial")
    public static class RunningEmbeddablesFailed extends RuntimeException {

        public RunningEmbeddablesFailed(String name, Throwable cause) {
            super("Failures in running embeddable " + name, cause);
        }

        public RunningEmbeddablesFailed(BatchFailures batchFailures) {
            super("Failures in running embeddables in batch: " + batchFailures);
        }

    }

    @SuppressWarnings("serial")
    public static class RunningStoriesFailed extends RuntimeException {

        public RunningStoriesFailed(int stories, int scenarios, int failedScenarios) {
            super("Failures in running " + stories + " stories containing " + scenarios + " scenarios (of which "
                    + failedScenarios + " failed)");
        }

        public RunningStoriesFailed(BatchFailures failures) {
            super("Failures in running stories in batch: " + failures);
        }

        public RunningStoriesFailed(String name, Throwable cause) {
            super("Failures in running stories " + name, cause);
        }
    }

    @SuppressWarnings("serial")
    public static class ViewGenerationFailed extends RuntimeException {
        public ViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewResources,
                RuntimeException cause) {
            super("View generation failed to " + outputDirectory + " for formats " + formats + " and resources "
                    + viewResources, cause);
        }

        public ViewGenerationFailed(File outputDirectory, StoryMaps storyMaps, Properties viewResources,
                RuntimeException cause) {
            super("View generation failed to " + outputDirectory + " for story maps " + storyMaps + " for resources "
                    + viewResources, cause);
        }

        public ViewGenerationFailed(File outputDirectory, Properties viewResources, RuntimeException e) {
            // TODO Auto-generated constructor stub
        }
    }

    /**
     * Non-threading ExecutorService for situations where thread count = 1
     */
    private static class NonThreadingExecutorService implements ExecutorService {
        public void shutdown() {
            throw new UnsupportedOperationException();
        }

        public List<Runnable> shutdownNow() {
            throw new UnsupportedOperationException();
        }

        public boolean isShutdown() {
            throw new UnsupportedOperationException();
        }

        public boolean isTerminated() {
            throw new UnsupportedOperationException();
        }

        public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        public <T> Future<T> submit(Callable<T> tCallable) {
            final Object[] rc = new Object[1];
            try {
                rc[0] = tCallable.call();
            } catch (Exception e) {
                rc[0] = e;
            }
            return new Future<T>() {

                public boolean cancel(boolean b) {
                    throw new UnsupportedOperationException();
                }

                public boolean isCancelled() {
                    throw new UnsupportedOperationException();
                }

                public boolean isDone() {
                    return true;
                }

                public T get() throws InterruptedException, ExecutionException {
                    return (T) rc[0];
                }

                public T get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                    return get();
                }
            };
        }

        public <T> Future<T> submit(Runnable runnable, T t) {
            throw new UnsupportedOperationException();
        }

        public Future<?> submit(Runnable runnable) {
            throw new UnsupportedOperationException();
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables, long l, TimeUnit timeUnit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> callables) throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException();
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> callables, long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException();
        }

        public void execute(Runnable runnable) {
            throw new UnsupportedOperationException();
        }
    }


}
