package org.jbehave.core.embedder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.MetaFilter.MetaMatcher;
import org.jbehave.core.embedder.StoryTimeouts.TimeoutParser;
import org.jbehave.core.embedder.executors.FixedThreadExecutors;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.failures.FailingUponPendingStep;
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
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Stepdoc;

/**
 * The Embedder is a facade allowing all functionality to be embedded into other
 * run contexts, such as IDEs (e.g. via JUnit support) or CLIs (via Ant or
 * Maven).
 */
public class Embedder {

	protected StoryMapper storyMapper;
	protected EmbedderMonitor embedderMonitor;
	protected EmbedderClassLoader classLoader;
	protected EmbedderControls embedderControls;
	protected EmbedderFailureStrategy embedderFailureStrategy;
    protected Configuration configuration;
    protected List<CandidateSteps> candidateSteps;
    protected InjectableStepsFactory stepsFactory;
    protected List<String> metaFilters;
    protected Map<String,MetaMatcher> metaMatchers;
    protected Properties systemProperties;
    protected ExecutorService executorService;
    protected boolean executorServiceCreated;
    protected PerformableTree performableTree;
    protected StoryManager storyManager;
    protected TimeoutParser[] timeoutParsers;

    public Embedder() {
        this(new PrintStreamEmbedderMonitor());
    }

    public Embedder(EmbedderMonitor embedderMonitor) {
        this(new StoryMapper(), new PerformableTree(), embedderMonitor);
    }

    public Embedder(StoryMapper storyMapper, PerformableTree performableTree, EmbedderMonitor embedderMonitor) {
        this.storyMapper = storyMapper;
        this.performableTree = performableTree;
        this.embedderMonitor = embedderMonitor;
    }

    public void mapStoriesAsPaths(List<String> storyPaths) {
        EmbedderControls embedderControls = embedderControls();
        embedderMonitor.usingControls(embedderControls);

        if (embedderControls.skip()) {
            embedderMonitor.storiesSkipped(storyPaths);
            return;
        }

        processSystemProperties();

        StoryManager storyManager = storyManager();
        for (String storyPath : storyPaths) {
            Story story = storyManager.storyOfPath(storyPath);
            embedderMonitor.mappingStory(storyPath, metaFilters());
            storyMapper.map(story, new MetaFilter("", embedderMonitor));
            for (String filter : metaFilters) {
                storyMapper.map(story, new MetaFilter(filter, embedderMonitor));
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
        embedderMonitor.usingControls(embedderControls);

        if (embedderControls.skip()) {
            embedderMonitor.embeddablesSkipped(classNames);
            return;
        }

        BatchFailures failures = new BatchFailures(embedderControls.verboseFailures());
        for (Embeddable embeddable : embeddables(classNames, classLoader())) {
            String name = embeddable.getClass().getName();
            try {
                embedderMonitor.runningEmbeddable(name);
                embeddable.useEmbedder(this);
                embeddable.run();
            } catch (Throwable e) {
                if (embedderControls.batch()) {
                    // collect and postpone decision to throw exception
                    failures.put(name, e);
                } else {
                    if (ignoreFailure(embedderControls)) {
                        embedderMonitor.embeddableFailed(name, e);
                    } else {
                        throw new RunningEmbeddablesFailed(name, e);
                    }
                }
            }
        }

        if (embedderControls.batch() && failures.size() > 0) {
            if (ignoreFailure(embedderControls)) {
                embedderMonitor.batchFailed(failures);
            } else {
                throw new RunningEmbeddablesFailed(failures);
            }
        }

    }

    private boolean ignoreFailure(EmbedderControls embedderControls) {
        boolean ignore = embedderControls.ignoreFailureInStories();
        if (embedderControls.generateViewAfterStories()) {
            ignore = ignore && embedderControls.ignoreFailureInView();
        }
        return ignore;
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
        for (String className : classNames) {
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

        EmbedderControls embedderControls = embedderControls();

        embedderMonitor.usingControls(embedderControls);

        if (embedderControls.skip()) {
            embedderMonitor.storiesSkipped(storyPaths);
            return;
        }

        try {

            // set up run context
            StoryManager storyManager = storyManager();
            MetaFilter filter = metaFilter();
            BatchFailures failures = new BatchFailures(embedderControls.verboseFailures());

            // run stories
            storyManager.runStoriesAsPaths(storyPaths, filter, failures);

            // handle any failures
            handleFailures(failures);

        } finally {
            // generate reports view regardless of failures in running stories
            // (if configured to do so)
            try {
                if (embedderControls.generateViewAfterStories()) {
                    generateReportsView();
                }
            } finally {
                // shutdown regardless of failures in reports view
                shutdownExecutorService();
            }

        }
    }

    private void handleFailures(BatchFailures failures) {
        if (failures.size() > 0) {
            if (embedderControls().ignoreFailureInStories()) {
                embedderMonitor.batchFailed(failures);
            } else {
                embedderFailureStrategy().handleFailures(failures);
            }
        }
    }

    public void generateReportsView() {
        StoryReporterBuilder builder = configuration().storyReporterBuilder();
        File outputDirectory = builder.outputDirectory();
        List<String> formatNames = builder.formatNames(true);
        generateReportsView(outputDirectory, formatNames, builder.viewResources());
    }

    public void generateReportsView(File outputDirectory, List<String> formats, Properties viewResources) {

        if (embedderControls().skip()) {
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
        handleFailures(count);

    }

    private void handleFailures(ReportsCount count) {
        boolean failed = count.failed();
        if (configuration().pendingStepStrategy() instanceof FailingUponPendingStep) {
            failed = failed || count.pending();
        }
        if (failed) {
            if (embedderControls().ignoreFailureInView()) {
                embedderMonitor.reportsViewFailures(count);
            } else {
                embedderFailureStrategy().handleFailures(count);
            }
        }
    }

    public void generateCrossReference() {
        StoryReporterBuilder builder = configuration().storyReporterBuilder();
        if (builder.hasCrossReference()) {
            builder.crossReference().serialise(storyManager().performableRoot(), builder.outputDirectory());
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
				Embedder configuredEmbedder = configurableEmbedder.configuredEmbedder();
				List<CandidateSteps> steps = configuredEmbedder.candidateSteps();
                if (steps.isEmpty()) {
                    steps = configuredEmbedder.stepsFactory().createCandidateSteps();
                }
                reportStepdocs(configuredEmbedder.configuration(), steps);
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
        if (classLoader == null) {
            this.classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        }
        return classLoader;
    }

    public Configuration configuration() {
        if (configuration == null) {
            this.configuration = new MostUsefulConfiguration();
        }
        configureThreads(configuration, embedderControls().threads());
        return configuration;
    }

    public List<CandidateSteps> candidateSteps() {
        if (candidateSteps == null) {
            candidateSteps = new ArrayList<CandidateSteps>();
        }
        return candidateSteps;
    }

    public InjectableStepsFactory stepsFactory() {
        if (stepsFactory == null) {
            stepsFactory = new ProvidedStepsFactory(candidateSteps());
        }
        return stepsFactory;
    }

    public EmbedderControls embedderControls() {
        if (embedderControls == null) {
            embedderControls = new EmbedderControls();
        }
        return embedderControls;
    }

    public EmbedderMonitor embedderMonitor() {
        return embedderMonitor;
    }

    public EmbedderFailureStrategy embedderFailureStrategy() {
        if (embedderFailureStrategy == null) {
            this.embedderFailureStrategy = new ThrowingRunningStoriesFailed();
        }
        return embedderFailureStrategy;
    }

    public boolean hasExecutorService() {
        return executorService != null;
    }

    public ExecutorService executorService() {
        if (executorService == null) {
            executorService = createExecutorService();
            executorServiceCreated = true;
        }
        return executorService;
    }

    /**
     * Creates a {@link ThreadPoolExecutor} using the number of threads defined
     * in the {@link EmbedderControls#threads()}
     * 
     * @return An ExecutorService
     */
    private ExecutorService createExecutorService() {
        return new FixedThreadExecutors().create(embedderControls());
    }

    /**
     * Shuts down executor service, if it was created by Embedder.
     * ExecutorServices provided by the
     * {@link #useExecutorService(ExecutorService)} need to be managed by the
     * provider.
     */
    protected void shutdownExecutorService() {
        if (executorServiceCreated) {
            executorService.shutdownNow();
            executorService = null;
            executorServiceCreated = false;
        }
    }

    public StoryManager storyManager() {
        if (storyManager == null) {
            storyManager = createStoryManager();
        }
        return storyManager;
    }

    private StoryManager createStoryManager() {
        return new StoryManager(configuration(), stepsFactory(), embedderControls(), embedderMonitor(),
                executorService(), performableTree(), timeoutParsers());
    }

    protected void configureThreads(Configuration configuration, int threads) {
        StoryReporterBuilder reporterBuilder = configuration.storyReporterBuilder();
        reporterBuilder.withMultiThreading(threads > 1);
        configuration.useStoryReporterBuilder(reporterBuilder);
    }

    public List<String> metaFilters() {
        if (metaFilters == null) {
            metaFilters = new ArrayList<String>();
        }
        return metaFilters;
    }

    public Map<String,MetaMatcher> metaMatchers(){
    	if (metaMatchers == null){
    		metaMatchers = new HashMap<String, MetaMatcher>();
    	}
    	return metaMatchers;
    }
    
    public MetaFilter metaFilter() {
        return new MetaFilter(StringUtils.join(metaFilters(), " "), embedderMonitor, metaMatchers());
    }

    public PerformableTree performableTree() {
        return performableTree;
    }

    public Properties systemProperties() {
        if (systemProperties == null) {
            systemProperties = new Properties();
        }
        return systemProperties;
    }
    
    public TimeoutParser[] timeoutParsers(){
    	if (timeoutParsers == null){
    		timeoutParsers = new TimeoutParser[]{};
    	}
    	return timeoutParsers;
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

    public void useEmbedderFailureStrategy(EmbedderFailureStrategy failureStategy) {
        this.embedderFailureStrategy = failureStategy;
    }

    public void useEmbedderMonitor(EmbedderMonitor embedderMonitor) {
        this.embedderMonitor = embedderMonitor;
    }

    public void useExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        embedderMonitor.usingExecutorService(executorService);
    }

    public void useMetaFilters(List<String> metaFilters) {
        this.metaFilters = metaFilters;
    }

    public void useMetaMatchers(Map<String,MetaMatcher> metaMatchers) {
        this.metaMatchers = metaMatchers;
    }

    public void usePerformableTree(PerformableTree performableTree) {
        this.performableTree = performableTree;
    }

    public void useSystemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
    }

    public void useTimeoutParsers(TimeoutParser... timeoutParsers){
		this.timeoutParsers = timeoutParsers;    	
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static interface EmbedderFailureStrategy {

        void handleFailures(BatchFailures failures);

        void handleFailures(ReportsCount count);

    }

    public static class ThrowingRunningStoriesFailed implements EmbedderFailureStrategy {

        public void handleFailures(BatchFailures failures) {
            throw new RunningStoriesFailed(failures);
        }

        public void handleFailures(ReportsCount count) {
            throw new RunningStoriesFailed(count);
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

        public RunningEmbeddablesFailed(String name, Throwable failure) {
            super("Failure in running embeddable: " + name, failure);
        }

        public RunningEmbeddablesFailed(BatchFailures failures) {
            super("Failures in running embeddables: " + failures);
        }

    }

    @SuppressWarnings("serial")
    public static class RunningStoriesFailed extends RuntimeException {

        public RunningStoriesFailed(ReportsCount reportsCount) {
            super("Failures in running stories: " + reportsCount);
        }

        public RunningStoriesFailed(BatchFailures failures) {
            super("Failures in running stories: " + failures);
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


}
