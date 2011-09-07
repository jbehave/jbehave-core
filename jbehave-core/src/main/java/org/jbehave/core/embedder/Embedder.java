package org.jbehave.core.embedder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryRunner.State;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.junit.AnnotatedEmbedderUtils;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.ReportsCount;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ProvidedStepsFactory;
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
    private InjectableStepsFactory stepsFactory;
    private EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
    private EmbedderControls embedderControls = new EmbedderControls();
    private List<String> metaFilters = Arrays.asList();
    private Properties systemProperties = new Properties();
    private StoryMapper storyMapper;
    private StoryRunner storyRunner;
    private EmbedderMonitor embedderMonitor;
    private ExecutorService executorService;

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
        Configuration configuration = configuration();
        StoryReporterBuilder builder = configuration.storyReporterBuilder();
        File outputDirectory = builder.outputDirectory();
        Properties viewResources = builder.viewResources();
        ViewGenerator viewGenerator = configuration.viewGenerator();
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

    public void runStoriesWithAnnotatedEmbedderRunner(List<String> classNames) {
        EmbedderClassLoader classLoader = classLoader();
        for (String className :  classNames) {
            embedderMonitor.runningWithAnnotatedEmbedderRunner(className);
            AnnotatedEmbedderRunner runner = AnnotatedEmbedderUtils.annotatedEmbedderRunner(className, classLoader);
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
    
    public void runStoriesAsPaths(List<String> storyPaths) {

        processSystemProperties();

        final EmbedderControls embedderControls = embedderControls();
        if (embedderControls.skip()) {
            embedderMonitor.storiesSkipped(storyPaths);
            return;
        }

        Configuration configuration = configuration();        
        InjectableStepsFactory stepsFactory = stepsFactory();
        List<CandidateSteps> candidateSteps = stepsFactory.createCandidateSteps();
        configureReporterBuilder(configuration);
        MetaFilter filter = new MetaFilter(StringUtils.join(metaFilters, " "), embedderMonitor);
        
        BatchFailures failures = new BatchFailures();

        State beforeStories = storyRunner.runBeforeOrAfterStories(configuration, candidateSteps, Stage.BEFORE);

        if ( storyRunner.failed(beforeStories) ){
            failures.put(beforeStories.toString(), storyRunner.failure(beforeStories));
        }

        List<Future<ThrowableStory>> futures = new ArrayList<Future<ThrowableStory>>();

        for (String storyPath : storyPaths) {
            enqueueStory(failures, filter, futures, storyPath, storyRunner.storyOfPath(configuration, storyPath), beforeStories);
        }

        waitUntilAllDoneOrFailed(futures, embedderControls, failures);

        State afterStories = storyRunner.runBeforeOrAfterStories(configuration, candidateSteps, Stage.AFTER);

        if ( storyRunner.failed(afterStories) ){
            failures.put(afterStories.toString(), storyRunner.failure(afterStories));
        }

        if ( failures.size() > 0) {
            if (embedderControls.ignoreFailureInStories()) {
                embedderMonitor.batchFailed(failures);
            } else {
                throw new RunningStoriesFailed(failures);
            }
        }

        if (embedderControls.generateViewAfterStories()) {
            generateReportsView();
        }
        
    }

    public Future<ThrowableStory> enqueueStory(BatchFailures batchFailures, MetaFilter filter,
            List<Future<ThrowableStory>> futures, String storyPath, Story story) {
        return enqueueStory(batchFailures, filter, futures, storyPath, story, null);
    }

    private Future<ThrowableStory> enqueueStory(BatchFailures batchFailures, MetaFilter filter,
            List<Future<ThrowableStory>> futures, String storyPath, Story story, State beforeStories) {
        EnqueuedStory enqueuedStory = enqueuedStory(embedderControls, configuration, stepsFactory, batchFailures,
                filter, storyPath, story, beforeStories);
        return submit(futures, enqueuedStory);
    }

    private synchronized Future<ThrowableStory> submit(List<Future<ThrowableStory>> futures, EnqueuedStory enqueuedStory) {
        if (executorService == null) {
            useExecutorService(createExecutorService());
        }
        Future<ThrowableStory> submit = executorService.submit(enqueuedStory);
        futures.add(submit);
        return submit;
    }

    protected EnqueuedStory enqueuedStory(EmbedderControls embedderControls, Configuration configuration,
            InjectableStepsFactory stepsFactory, BatchFailures batchFailures, MetaFilter filter, String storyPath,
            Story story, State beforeStories) {
        return new EnqueuedStory(storyPath, configuration, stepsFactory, story, filter, embedderControls,
                batchFailures, embedderMonitor, storyRunner, beforeStories);
    }

    /**
     * Creates a {@link ThreadPoolExecutor} using the number of threads defined
     * in the {@link EmbedderControls#threads()}
     * 
     * @return An ExecutorService
     */
    protected ExecutorService createExecutorService() {
        int threads = embedderControls.threads();
        embedderMonitor.usingThreads(threads);
        return Executors.newFixedThreadPool(threads);
    }

    private void waitUntilAllDoneOrFailed(List<Future<ThrowableStory>> futures, EmbedderControls embedderControls, BatchFailures failures) {
        long start = System.currentTimeMillis();
        boolean allDone = false;
        while (!allDone) {
            allDone = true;
            for (Future<ThrowableStory> future : futures) {
                if (!future.isDone()) {
                    allDone = false;
                    long durationInSecs = storyDurationInSecs(start);
                    if (durationInSecs > embedderControls.storyTimeoutInSecs()) {
                        Story story = null;
                        try {
                            story = future.get().getStory();
                        } catch (Throwable e) {
                            failures.put(future.toString(), e);
                            if (!embedderControls.ignoreFailureInStories()) {
                                break;
                            }
                        }
                        embedderMonitor.storyTimeout(durationInSecs, story);
                        future.cancel(true);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    break;
                } else {
                    try {
                        ThrowableStory throwableStory = future.get();
                        if (throwableStory.throwable != null) {
                            failures.put(future.toString(), throwableStory.throwable);
                            if (!embedderControls.ignoreFailureInStories()) {
                                break;
                            }
                        }
                    } catch (Throwable e) {
                        failures.put(future.toString(), e);
                        if (!embedderControls.ignoreFailureInStories()) {
                            break;
                        }
                    }
                }
            }
        }
        // cancel any outstanding execution which is not done before returning
        for (Future<ThrowableStory> future : futures) {
            if ( !future.isDone() ){
                future.cancel(true);
            }
        }
    }

    private long storyDurationInSecs(long start) {
        return (System.currentTimeMillis() - start) / 1000;
    }

    private void configureReporterBuilder(Configuration configuration) {
        StoryReporterBuilder reporterBuilder = configuration.storyReporterBuilder();
        reporterBuilder.withMultiThreading(embedderControls.threads() > 1);
        configuration.useStoryReporterBuilder(reporterBuilder);
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
        if (!embedderControls.ignoreFailureInView() && count.failed() ) {
            throw new RunningStoriesFailed(count);
        }

    }

    public void generateCrossReference() {
        StoryReporterBuilder builder = configuration().storyReporterBuilder();
        if (builder.hasCrossReference()){
            builder.crossReference().outputToFiles(builder);
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
                List<CandidateSteps> steps = configurableEmbedder.candidateSteps();
                if ( steps.isEmpty() ){
                    steps = configurableEmbedder.stepsFactory().createCandidateSteps();
                }
                reportStepdocs(configurableEmbedder.configuration(), steps);
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

    public InjectableStepsFactory stepsFactory() {
        if ( stepsFactory == null ){
            stepsFactory = new ProvidedStepsFactory(candidateSteps);            
        }
        return stepsFactory;
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

    public void useStepsFactory(InjectableStepsFactory stepsFactory) {
        this.stepsFactory = stepsFactory;        
    }

    public void useEmbedderControls(EmbedderControls embedderControls) {
        this.embedderControls = embedderControls;
    }

    public void useEmbedderMonitor(EmbedderMonitor embedderMonitor) {
        this.embedderMonitor = embedderMonitor;
    }

    public void useExecutorService(ExecutorService executorService){
        this.executorService = executorService;        
        embedderMonitor.usingExecutorService(executorService);
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

        public RunningStoriesFailed(ReportsCount count) {
            super("Failures in running stories: " + count);
        }

        public RunningStoriesFailed(BatchFailures failures) {
            super("Failures in running stories in batch: " + failures);
        }

        public RunningStoriesFailed(String name, Throwable cause) {
            super("Failures in running stories " + name, cause);
        }

        public RunningStoriesFailed() {
            super("Failures in running before or after stories steps");
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
    }

    public static class EnqueuedStory implements Callable<ThrowableStory> {
        private final String storyPath;
        private final Configuration configuration;
        private final InjectableStepsFactory stepsFactory;
        private final Story story;
        private final MetaFilter filter;
        private final EmbedderControls embedderControls;
        private final BatchFailures batchFailures;
        private final EmbedderMonitor embedderMonitor;
        private final StoryRunner storyRunner;
        private final State beforeStories;

        public EnqueuedStory(String storyPath, Configuration configuration, List<CandidateSteps> candidateSteps,
                Story story, MetaFilter filter, EmbedderControls embedderControls, BatchFailures batchFailures,
                EmbedderMonitor embedderMonitor, StoryRunner storyRunner, State beforeStories) {
            this(storyPath, configuration, new ProvidedStepsFactory(candidateSteps), story, filter, embedderControls, batchFailures, embedderMonitor, storyRunner, beforeStories);
        }

        public EnqueuedStory(String storyPath, Configuration configuration, InjectableStepsFactory stepsFactory,
                Story story, MetaFilter filter, EmbedderControls embedderControls, BatchFailures batchFailures,
                EmbedderMonitor embedderMonitor, StoryRunner storyRunner, State beforeStories) {
            this.storyPath = storyPath;
            this.configuration = configuration;
            this.stepsFactory = stepsFactory;
            this.story = story;
            this.filter = filter;
            this.embedderControls = embedderControls;
            this.batchFailures = batchFailures;
            this.embedderMonitor = embedderMonitor;
            this.storyRunner = storyRunner;
            this.beforeStories = beforeStories;
        }

        public ThrowableStory call() throws Exception {
            try {
                embedderMonitor.runningStory(storyPath);
                storyRunner.run(configuration, stepsFactory, story, filter, beforeStories);
            } catch (Throwable e) {
                if (embedderControls.batch()) {
                    // collect and postpone decision to throw exception
                    batchFailures.put(storyPath, e);
                } else {
                    if (embedderControls.ignoreFailureInStories()) {
                        embedderMonitor.storyFailed(storyPath, e);
                    } else {
                        return new ThrowableStory(new RunningStoriesFailed(storyPath, e), story);
                    }
                }
            }
            return new ThrowableStory(null, story);
        }
    }
    
    public static class ThrowableStory {
        private Throwable throwable;
        private Story story;

        public ThrowableStory(Throwable throwable, Story story) {
            this.throwable = throwable;
            this.story = story;
        }

        public Story getStory() {
            return story;
        }
    }

}
