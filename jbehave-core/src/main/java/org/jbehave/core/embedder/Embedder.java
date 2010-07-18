package org.jbehave.core.embedder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Stepdoc;

/**
 * Represents an entry point to all of JBehave's functionality that is
 * embeddable into other lanchers, such as IDEs or CLIs.
 */
public class Embedder {

    private Configuration configuration = new MostUsefulConfiguration();
    private List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
    private EmbedderControls embedderControls = new EmbedderControls();
    private StoryRunner storyRunner;
    private EmbedderMonitor embedderMonitor;

    public Embedder() {
        this(new StoryRunner(), new PrintStreamEmbedderMonitor());
    }

    public Embedder(StoryRunner storyRunner, EmbedderMonitor embedderMonitor) {
        this.storyRunner = storyRunner;
        this.embedderMonitor = embedderMonitor;
    }

    public void runStoriesAsEmbeddables(List<String> classNames, EmbedderClassLoader classLoader) {
        EmbedderControls embedderControls = embedderControls();
        if (embedderControls.skip()) {
            embedderMonitor.storiesNotRun();
            return;
        }

        Map<String, Throwable> failedStories = new HashMap<String, Throwable>();
        for (Embeddable embeddable : embeddables(classNames, classLoader)) {
            String name = embeddable.getClass().getName();
            try {
                embedderMonitor.runningStory(name);
                embeddable.useEmbedder(this);
                embeddable.run();
            } catch (Throwable e) {
                if (embedderControls.batch()) {
                    // collect and postpone decision to throw exception
                    failedStories.put(name, e);
                } else {
                    if (embedderControls.ignoreFailureInStories()) {
                        embedderMonitor.storyFailed(name, e);
                    } else {
                        throw new RunningStoriesFailed(name, e);
                    }
                }
            }
        }

        if (embedderControls.batch() && failedStories.size() > 0) {
            if (embedderControls.ignoreFailureInStories()) {
                embedderMonitor.storiesBatchFailed(format(failedStories));
            } else {
                throw new RunningStoriesFailed(failedStories);
            }
        }

        if (embedderControls.generateViewAfterStories()) {
            generateStoriesView();
        }

    }

    private List<Embeddable> embeddables(List<String> classNames, EmbedderClassLoader classLoader) {
        List<Embeddable> embeddables = new ArrayList<Embeddable>();
        for (String className : classNames) {
            embeddables.add(classLoader.newInstance(Embeddable.class, className));
        }
        return embeddables;
    }

    public void buildReporters(Configuration configuration, List<String> storyPaths) {
        StoryReporterBuilder reporterBuilder = configuration.storyReporterBuilder();
        configuration.useStoryReporters(reporterBuilder.build(storyPaths));
    }

    public void runStoriesAsPaths(List<String> storyPaths) {
        EmbedderControls embedderControls = embedderControls();
        if (embedderControls.skip()) {
            embedderMonitor.storiesNotRun();
            return;
        }

        Map<String, Throwable> failedStories = new HashMap<String, Throwable>();
        Configuration configuration = configuration();
        buildReporters(configuration, storyPaths);
        for (String storyPath : storyPaths) {
            try {
                embedderMonitor.runningStory(storyPath);
                Story story = storyRunner.storyOfPath(configuration, storyPath);
                storyRunner.run(configuration, candidateSteps(), story);
            } catch (Throwable e) {
                if (embedderControls.batch()) {
                    // collect and postpone decision to throw exception
                    failedStories.put(storyPath, e);
                } else {
                    if (embedderControls.ignoreFailureInStories()) {
                        embedderMonitor.storyFailed(storyPath, e);
                    } else {
                        throw new RunningStoriesFailed(storyPath, e);
                    }
                }
            }
        }

        if (embedderControls.batch() && failedStories.size() > 0) {
            if (embedderControls.ignoreFailureInStories()) {
                embedderMonitor.storiesBatchFailed(format(failedStories));
            } else {
                throw new RunningStoriesFailed(failedStories);
            }
        }

        if (embedderControls.generateViewAfterStories()) {
            generateStoriesView();
        }

    }

    public void runStoriesWithAnnotatedEmbedderRunner(String runnerClass, List<String> classNames,
            EmbedderClassLoader classLoader) {
        List<AnnotatedEmbedderRunner> runners = annotatedEmbedderRunners(runnerClass, classNames, classLoader);
        for (AnnotatedEmbedderRunner runner : runners) {
            try {
                Object annotatedInstance = runner.createTest();
                if (annotatedInstance instanceof Embeddable) {
                    ((Embeddable) annotatedInstance).run();
                } else {
                    embedderMonitor.annotatedInstanceNotOfType(annotatedInstance, Embeddable.class);
                }
            } catch (Throwable e) {
                throw new RuntimeException(runner.toString(), e);
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
            throw new RuntimeException(e);
        }
    }

    private Class<?> loadClass(String className, EmbedderClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
            embedderMonitor.storiesViewNotGenerated();
            return;
        }
        ViewGenerator viewGenerator = configuration().viewGenerator();
        try {
            embedderMonitor.generatingStoriesView(outputDirectory, formats, viewResources);
            viewGenerator.generateView(outputDirectory, formats, viewResources);
        } catch (RuntimeException e) {
            embedderMonitor.storiesViewGenerationFailed(outputDirectory, formats, viewResources, e);
            String message = "Failed to generate stories view in " + outputDirectory + " with formats " + formats
                    + " and resources " + viewResources;
            throw new ViewGenerationFailed(message, e);
        }
        int stories = viewGenerator.countStories();
        int scenarios = viewGenerator.countScenarios();
        int failedScenarios = viewGenerator.countFailedScenarios();
        embedderMonitor.storiesViewGenerated(stories, scenarios, failedScenarios);
        if (!embedderControls.ignoreFailureInView() && failedScenarios > 0) {
            throw new RunningStoriesFailed(stories, scenarios, failedScenarios);
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

    public StoryRunner storyRunner() {
        return storyRunner;
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

    public void useStoryRunner(StoryRunner storyRunner) {
        this.storyRunner = storyRunner;
    }

    private String format(Map<String, Throwable> failedStories) {
        StringBuffer sb = new StringBuffer();
        for (String storyName : failedStories.keySet()) {
            Throwable cause = failedStories.get(storyName);
            sb.append("\n");
            sb.append(storyName);
            sb.append(": ");
            sb.append(cause.getMessage());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @SuppressWarnings("serial")
    public class RunningStoriesFailed extends RuntimeException {

        public RunningStoriesFailed(int stories, int scenarios, int failedScenarios) {
            super("Failures in running " + stories +" stories containing "+ scenarios + " scenarios (of which " + failedScenarios
            + " failed)");
        }

        public RunningStoriesFailed(Map<String, Throwable> failedStories) {
            super("Failures in running stories in batch: " + format(failedStories));
        }

        public RunningStoriesFailed(String name, Throwable cause) {
            super("Failures in running story " + name, cause);
        }
    }

    @SuppressWarnings("serial")
    public class ViewGenerationFailed extends RuntimeException {
        public ViewGenerationFailed(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
