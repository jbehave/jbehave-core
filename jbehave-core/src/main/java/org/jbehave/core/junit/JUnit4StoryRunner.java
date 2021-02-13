package org.jbehave.core.junit;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class JUnit4StoryRunner extends BlockJUnit4ClassRunner {
    private final ConfigurableEmbedder configurableEmbedder;
    private final Embedder configuredEmbedder;
    private final Configuration configuration;
    private final Description description;
    private int numberOfTestCases;

    public JUnit4StoryRunner(Class<? extends ConfigurableEmbedder> testClass)
            throws InitializationError, ReflectiveOperationException {
        super(testClass);
        configurableEmbedder = testClass.getDeclaredConstructor().newInstance();
        configuredEmbedder = configurableEmbedder.configuredEmbedder();
        configuration = configuredEmbedder.configuration();

        List<String> storyPaths = storyPathsFor(configurableEmbedder);

        StepMonitor originalStepMonitor = configuration.stepMonitor();
        configuration.useStepMonitor(new NullStepMonitor());
        List<Description> storyDescriptions = buildDescriptionsFromStories(storyPaths);
        configuration.useStepMonitor(originalStepMonitor);

        description = Description.createSuiteDescription(testClass);
        for (Description storyDescription : storyDescriptions) {
            description.addChild(storyDescription);
        }
    }

    private List<String> storyPathsFor(ConfigurableEmbedder configurableEmbedder) {
        if (configurableEmbedder instanceof JUnitStories) {
            return ((JUnitStories)configurableEmbedder).storyPaths();
        }
        throw new IllegalArgumentException(
                "Only ConfigurableEmbedder of type JUnitStories is supported");
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public int testCount() {
        return numberOfTestCases;
    }

    /**
     * Returns a {@link Statement} execution the {@link ConfigurableEmbedder#run()}
     * with a {@link JUnit4StoryReporter}.
     */
    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() {
                JUnit4StoryReporter reporter = new JUnit4StoryReporter(notifier, description,
                        configuration.keywords());
                // tell the reporter how to handle pending steps
                reporter.usePendingStepStrategy(configuration.pendingStepStrategy());

                addToStoryReporterFormats(reporter);

                configurableEmbedder.run();
            }
        };
    }

    public static EmbedderControls recommendedControls(Embedder embedder) {
        return embedder.embedderControls()
                // don't throw an exception on generating reports for failing stories
                .doIgnoreFailureInView(true)
                // don't throw an exception when a story failed
                .doIgnoreFailureInStories(true);
    }

    private List<CandidateSteps> getCandidateSteps() {
        InjectableStepsFactory stepsFactory = configurableEmbedder.stepsFactory();
        if (stepsFactory != null) {
            return stepsFactory.createCandidateSteps();
        }
        return configuredEmbedder.stepsFactory().createCandidateSteps();
    }

    private void addToStoryReporterFormats(StoryReporter reporter) {
        StoryReporterBuilder storyReporterBuilder = configuration.storyReporterBuilder();
        storyReporterBuilder.withFormats(new StoryReporterBuilder.ProvidedFormat(reporter));
    }

    private List<Description> buildDescriptionsFromStories(List<String> storyPaths) {
        List<CandidateSteps> candidateSteps = getCandidateSteps();
        JUnit4DescriptionGenerator descriptionGenerator = new JUnit4DescriptionGenerator(candidateSteps, configuration);
        List<Description> storyDescriptions = new ArrayList<>();

        addSuite(storyDescriptions, "BeforeStories");
        PerformableTree performableTree = createPerformableTree(candidateSteps, storyPaths);
        storyDescriptions.addAll(descriptionGenerator.createDescriptionsFrom(performableTree));
        addSuite(storyDescriptions, "AfterStories");

        numberOfTestCases += descriptionGenerator.getTestCases();

        return storyDescriptions;
    }

    private PerformableTree createPerformableTree(List<CandidateSteps> candidateSteps, List<String> storyPaths) {
        BatchFailures failures = new BatchFailures(configuredEmbedder.embedderControls().verboseFailures());
        PerformableTree performableTree = configuredEmbedder.performableTree();
        RunContext context = performableTree.newRunContext(configuration, candidateSteps,
                configuredEmbedder.embedderMonitor(), configuredEmbedder.metaFilter(), failures);
        performableTree.addStories(context, configuredEmbedder.storyManager().storiesOfPaths(storyPaths));
        return performableTree;
    }

    private void addSuite(List<Description> storyDescriptions, String name) {
        storyDescriptions.add(Description.createTestDescription(Object.class, name));
        numberOfTestCases++;
    }
}
