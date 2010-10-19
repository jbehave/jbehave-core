package org.jbehave.core.embedder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryMap;
import org.jbehave.core.reporters.ReportsCount;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Stepdoc;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * Represents an entry point to all of JBehave's functionality that is
 * embeddable into other launchers, such as IDEs or CLIs.
 */
public class Embedder {

    private Configuration configuration = new MostUsefulConfiguration();
    private List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
    private EmbedderControls embedderControls = new EmbedderControls();
    private StoryRunner storyRunner;
    private EmbedderMonitor embedderMonitor;
    private List<String> metaFilters = Arrays.asList();
    private EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());

    public Embedder() {
        this(new StoryRunner(), new PrintStreamEmbedderMonitor());
    }

    public Embedder(StoryRunner storyRunner, EmbedderMonitor embedderMonitor) {
        this.storyRunner = storyRunner;
        this.embedderMonitor = embedderMonitor;
    }

    public void mapStoriesAsPaths(List<String> storyPaths) {
        EmbedderControls embedderControls = embedderControls();
        if (embedderControls.skip()) {
            embedderMonitor.storiesSkipped(storyPaths);
            return;
        }

        StoryMapper storyMapper = new StoryMapper();
        for (String storyPath : storyPaths) {
            Story story = storyRunner.storyOfPath(configuration, storyPath);
            embedderMonitor.mappingStory(storyPath, metaFilters);
            storyMapper.map(story, new MetaFilter(""));
            for (String filter : metaFilters) {
                storyMapper.map(story, new MetaFilter(filter));
            }
        }

        generateStoryMapsView(storyMapper.getStoryMaps());

    }

    private void generateStoryMapsView(List<StoryMap> storyMaps) {
        StoryReporterBuilder builder = configuration().storyReporterBuilder();
        File outputDirectory = builder.outputDirectory();
        ViewGenerator viewGenerator = configuration().viewGenerator();
        viewGenerator.generateMapsView(outputDirectory, storyMaps, builder.viewResources());
    }

    public void runStoriesAsEmbeddables(List<String> classNames) {
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
                        throw new RunningStoriesFailed(name, e);
                    }
                }
            }
        }

        if (embedderControls.batch() && batchFailures.size() > 0) {
            if (embedderControls.ignoreFailureInStories()) {
                embedderMonitor.batchFailed(batchFailures);
            } else {
                throw new RunningStoriesFailed(batchFailures);
            }
        }

        if (embedderControls.generateViewAfterStories()) {
            generateStoriesView();
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
        EmbedderControls embedderControls = embedderControls();
        if (embedderControls.skip()) {
            embedderMonitor.storiesSkipped(storyPaths);
            return;
        }

        Configuration configuration = configuration();
        List<CandidateSteps> candidateSteps = candidateSteps();

        storyRunner.runBeforeOrAfterStories(configuration, candidateSteps, Stage.BEFORE);

        BatchFailures batchFailures = new BatchFailures();
        buildReporters(configuration, storyPaths);
        MetaFilter filter = new MetaFilter(StringUtils.join(metaFilters, " "), embedderMonitor);
        for (String storyPath : storyPaths) {
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
                        throw new RunningStoriesFailed(storyPath, e);
                    }
                }
            }
        }

        storyRunner.runBeforeOrAfterStories(configuration, candidateSteps, Stage.AFTER);

        if (embedderControls.batch() && batchFailures.size() > 0) {
            if (embedderControls.ignoreFailureInStories()) {
                embedderMonitor.batchFailed(batchFailures);
            } else {
                throw new RunningStoriesFailed(batchFailures);
            }
        }

        if (embedderControls.generateViewAfterStories()) {
            generateStoriesView();
        }

    }

    private void buildReporters(Configuration configuration, List<String> storyPaths) {
        StoryReporterBuilder reporterBuilder = configuration.storyReporterBuilder();
        configuration.useStoryReporters(reporterBuilder.build(storyPaths));
    }

    public void generateStoriesView() {
        StoryReporterBuilder builder = configuration().storyReporterBuilder();
        File outputDirectory = builder.outputDirectory();
        List<String> formatNames = builder.formatNames(true);
        generateStoriesView(outputDirectory, formatNames, builder.viewResources());
    }

    public void generateStoriesView(File outputDirectory, List<String> formats, Properties viewResources) {
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

    public void reportStepdocs() {
        Configuration configuration = configuration();
        List<CandidateSteps> candidateSteps = candidateSteps();
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

    public EmbedderClassLoader classLoader(){
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
    public static class RunningStoriesFailed extends RuntimeException {

        public RunningStoriesFailed(int stories, int scenarios, int failedScenarios) {
            super("Failures in running " + stories + " stories containing " + scenarios + " scenarios (of which "
                    + failedScenarios + " failed)");
        }

        public RunningStoriesFailed(BatchFailures failures) {
            super("Failures in running stories in batch: " + failures);
        }

        public RunningStoriesFailed(String name, Throwable cause) {
            super("Failures in running story " + name, cause);
        }
    }

    @SuppressWarnings("serial")
    public static class ViewGenerationFailed extends RuntimeException {
        public ViewGenerationFailed(File outputDirectory, List<String> formats, Properties viewResources,
                RuntimeException cause) {
            super("View generation failed to " + outputDirectory + " for formats " + formats + " and resources "
                    + viewResources, cause);
        }
    }
}
