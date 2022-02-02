package org.jbehave.core.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.Pair;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.junit.JUnit4DescriptionGenerator.JUnit4Test;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.NullStoryReporter;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnit4StoryReporter extends NullStoryReporter {
    private final RunNotifier notifier;

    private final Description rootDescription;
    private final Keywords keywords;
    private PendingStepStrategy pendingStepStrategy = new PassingUponPendingStep();

    private final ThreadLocal<TestState> testState = ThreadLocal.withInitial(TestState::new);

    private final AtomicInteger testCounter = new AtomicInteger();

    public JUnit4StoryReporter(RunNotifier notifier, Description rootDescription, Keywords keywords) {
        this.rootDescription = rootDescription;
        this.notifier = notifier;
        this.keywords = keywords;
    }

    @Override
    public void beforeStoriesSteps(StepCollector.Stage stage) {
        String name = null;
        if (stage == StepCollector.Stage.BEFORE) {
            notifier.fireTestRunStarted(rootDescription);
            name = "BeforeStories";
        } else if (stage == StepCollector.Stage.AFTER) {
            name = "AfterStories";
        }

        Description storyDescription = findStoryDescription(name);
        TestState testState = this.testState.get();
        testState.currentStoryDescription = storyDescription;
        testState.currentStep = storyDescription;
        notifier.fireTestStarted(storyDescription);

    }

    @Override
    public void afterStoriesSteps(StepCollector.Stage stage) {
        TestState testState = this.testState.get();
        notifier.fireTestFinished(testState.currentStoryDescription);
        if (stage == StepCollector.Stage.AFTER) {
            Result result = new Result();
            notifier.fireTestRunFinished(result);
        }
    }

    @Override
    public void beforeStory(Story story, boolean isGivenStory) {
        TestState testState = this.testState.get();
        if (isGivenStory) {
            if (testState.currentStep != null) {
                notifier.fireTestStarted(testState.currentStep);
            }
            testState.givenStoryLevel++;
        } else {
            Description storyDescription = findStoryDescription(story.getName());
            testState.currentStoryDescription = storyDescription;
            notifier.fireTestStarted(storyDescription);

            if (storyDescription.isSuite()) {
                testState.scenarioDescriptions = filter(storyDescription.getChildren(), ImmutableList.of(
                    Pair.of(ElementAction.DROP, isTest()),
                    Pair.of(ElementAction.TAKE, isSuite())
                )).iterator();

                testState.moveToNextScenario();
            }

            testState.currentStep = testState.currentStoryDescription;
        }
    }

    @Override
    public void beforeStorySteps(Stage stage, Lifecycle.ExecutionType type) {
        TestState testState = this.testState.get();
        if (!testState.isGivenStoryRunning()) {

            if (stage == Stage.BEFORE && type == Lifecycle.ExecutionType.SYSTEM) {
                loadStepDescriptions(filter(testState.getStoryChildren(), ImmutableList.of(
                    Pair.of(ElementAction.TAKE, isTest())
                )));
            }

            if (stage == Stage.AFTER && type == Lifecycle.ExecutionType.USER) {
                loadStepDescriptions(filter(testState.getStoryChildren(), ImmutableList.of(
                    Pair.of(ElementAction.DROP, isTest()),
                    Pair.of(ElementAction.DROP, isSuite()),
                    Pair.of(ElementAction.TAKE, isTest())
                )));
            }
        }
    }

    private void loadStepDescriptions(List<Description> stepDescriptions) {
        TestState testState = this.testState.get();
        testState.loadStepDescriptions(stepDescriptions);

        if (testState.stepDescriptions.hasNext()) {
            testState.moveToNextStep();
        }
    }

    private Predicate<Description> isTest() {
        return description -> description.getAnnotation(JUnit4Test.class) != null;
    }

    private Predicate<Description> isSuite() {
        return isTest().negate();
    }

    private List<Description> filter(List<Description> descriptions,
            Collection<Pair<ElementAction, Predicate<Description>>> filters) {
        Iterator<Pair<ElementAction, Predicate<Description>>> filtersIterator = filters.iterator();
        Pair<ElementAction, Predicate<Description>> currentFilter = filtersIterator.next();

        List<Description> resultDescriptions = new ArrayList<>();
        for (Description description : descriptions) {
            while (!currentFilter.getValue().test(description)) {
                if (!filtersIterator.hasNext()) {
                    return resultDescriptions;
                }
                currentFilter = filtersIterator.next();
            }

            currentFilter.getKey().compute(resultDescriptions, description);
        }

        return resultDescriptions;
    }

    private Description findStoryDescription(String storyName) {
        String escapedStoryName = TextManipulator.escape(storyName);
        for (Description storyDescription : rootDescription.getChildren()) {
            if (storyDescription.getDisplayName().equals(escapedStoryName)) {
                return storyDescription;
            } else
                // Related to issue #28: When a story does not contain any scenarios, isTest returns true,
                // but getMethodName still returns null, because it cannot be parsed by JUnit as a method name.
                if (storyDescription.isTest() && storyDescription.getMethodName() != null && storyDescription
                        .getMethodName().equals(storyName)) {
                    // Story BeforeStories or AfterStories
                    return storyDescription;
                }
        }
        throw new IllegalStateException("No JUnit description found for story with name: " + storyName);
    }

    @Override
    public void afterStory(boolean isGivenStory) {
        TestState testState = this.testState.get();
        if (isGivenStory) {
            testState.givenStoryLevel--;
            if (testState.currentStep != null) {
                notifier.fireTestFinished(testState.currentStep);
            }
            prepareNextStep();
        } else {
            if (!testState.failedSteps.contains(testState.currentStoryDescription)) {
                notifier.fireTestFinished(testState.currentStoryDescription);
                if (testState.currentStoryDescription.isTest()) {
                    testCounter.incrementAndGet();
                }
            }
            this.testState.remove();
        }
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        TestState testState = this.testState.get();
        if (!testState.isGivenStoryRunning()) {
            notifier.fireTestStarted(testState.currentScenario);

            List<Description> children = testState.currentScenario.getChildren();
            List<Description> examples = filterExamples(children);
            if (!examples.isEmpty()) {
                testState.exampleDescriptions = examples.iterator();
                testState.currentExample = null;
            }
            if (children.size() > examples.size()) {
                // in case of given stories, these steps are actually stories,
                // for which events will be fired in beforeStory(..., true)
                List<Description> steps = new ArrayList<>(testState.currentScenario.getChildren());
                steps.removeAll(examples);
                testState.loadStepDescriptions(steps);
                testState.moveToNextStep();
            }
        }
    }

    private List<Description> filterExamples(List<Description> children) {
        for (int i = 0; i < children.size(); i++) {
            Description child = children.get(i);
            boolean isExample = child.getDisplayName().startsWith(keywords.examplesTableRow() + " ");
            if (isExample) {
                return children.subList(i, children.size());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void afterScenario(Timing timing) {
        TestState testState = this.testState.get();
        if (!testState.isGivenStoryRunning()) {
            notifier.fireTestFinished(testState.currentScenario);
            testState.moveToNextScenario();
        }
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        TestState testState = this.testState.get();
        if (!testState.isGivenStoryRunning()) {
            testState.moveToNextExample();
            testState.loadStepDescriptions(testState.currentExample.getChildren());
            testState.moveToNextStep();
        }
    }

    @Override
    public void beforeStep(Step step) {
        if (step.getExecutionType() != StepExecutionType.EXECUTABLE) {
            return;
        }
        TestState testState = this.testState.get();
        if (!testState.isGivenStoryRunning() && testState.currentStep != null) {
            // Lifecycle Before story steps
            if (testState.currentStep == testState.currentStoryDescription) {
                testState.currentStep = testState.currentScenario;
            }
            if (testState.currentStepStatus == StepStatus.STARTED) {
                testState.parentSteps.push(testState.currentStep);
                testState.moveToNextStep();
            }
            notifier.fireTestStarted(testState.currentStep);
            testState.currentStepStatus = StepStatus.STARTED;
        }
    }

    @Override
    public void failed(String step, Throwable e) {
        TestState testState = this.testState.get();
        if (!testState.isGivenStoryRunning()) {
            Throwable thrownException = e instanceof UUIDExceptionWrapper ? e.getCause() : e;
            notifier.fireTestFailure(new Failure(testState.currentStep, thrownException));
            testState.failedSteps.add(testState.currentStep);
            finishStep(testState);
        }
    }

    @Override
    public void successful(String step) {
        TestState testState = this.testState.get();
        if (!testState.isGivenStoryRunning()) {
            if (testState.currentStep != null) {
                finishStep(testState);
            } else {
                prepareNextStep();
            }
        }
    }

    private void prepareNextStep() {
        TestState testState = this.testState.get();
        if (testState.currentStep != null && testState.currentStep.isTest()) {
            testCounter.incrementAndGet();
        }
        if (testState.stepDescriptions != null && testState.stepDescriptions.hasNext()) {
            testState.moveToNextStep();
        }
    }

    private void finishStep(TestState testState) {
        if (testState.currentStepStatus == StepStatus.FINISHED && !testState.parentSteps.isEmpty()) {
            notifier.fireTestFinished(testState.parentSteps.poll());
        } else {
            notifier.fireTestFinished(testState.currentStep);
            testState.currentStepStatus = StepStatus.FINISHED;
            prepareNextStep();
        }
    }

    @Override
    public void pending(String step) {
        TestState testState = this.testState.get();
        if (!testState.isGivenStoryRunning()) {
            if (pendingStepStrategy instanceof FailingUponPendingStep) {
                notifier.fireTestStarted(testState.currentStep);
                notifier.fireTestFailure(new Failure(testState.currentStep, new RuntimeException("Step is pending!")));
                // Pending step strategy says to fail so treat this step as
                // having failed.
                testState.failedSteps.add(testState.currentStep);
                finishStep(testState);
            } else {
                notifier.fireTestIgnored(testState.currentStep);
                prepareNextStep();
            }
        }
    }

    @Override
    public void ignorable(String step) {
        TestState testState = this.testState.get();
        if (!testState.isGivenStoryRunning()) {
            notifier.fireTestIgnored(testState.currentStep);
            testState.currentStepStatus = StepStatus.FINISHED;
            prepareNextStep();
        }
    }

    @Override
    public void notPerformed(String step) {
        ignorable(step);
    }

    /**
     * Notify the IDE that the current step and scenario is not being executed.
     * Reason is a JBehave meta tag is filtering out this scenario.
     *
     * @param scenario Scenario
     * @param filter   Filter
     */
    @Override
    public void scenarioExcluded(Scenario scenario, String filter) {
        TestState testState = this.testState.get();
        notifier.fireTestIgnored(testState.currentStep);
        notifier.fireTestIgnored(testState.currentScenario);
    }

    public void usePendingStepStrategy(PendingStepStrategy pendingStepStrategy) {
        this.pendingStepStrategy = pendingStepStrategy;
    }

    private class TestState {
        private Description currentStep;
        private StepStatus currentStepStatus;
        private final Deque<Description> parentSteps = new LinkedList<>();
        private Iterator<Description> stepDescriptions;

        private Description currentScenario;
        private Iterator<Description> scenarioDescriptions;

        private Description currentExample;
        private Iterator<Description> exampleDescriptions;

        private Description currentStoryDescription;
        private int givenStoryLevel;

        private final Set<Description> failedSteps = new HashSet<>();

        private void moveToNextScenario() {
            currentScenario = getNextOrNull(scenarioDescriptions);
            currentStep = currentScenario;
            stepDescriptions = null;
        }

        private void moveToNextExample() {
            currentExample = getNextOrNull(exampleDescriptions);
        }

        private void moveToNextStep() {
            currentStep = getNextOrNull(stepDescriptions);
        }

        private boolean isGivenStoryRunning() {
            return givenStoryLevel != 0;
        }

        private void loadStepDescriptions(List<Description> steps) {
            stepDescriptions = getAllDescendants(steps).iterator();
        }

        private <T> T getNextOrNull(Iterator<T> iterator) {
            return iterator.hasNext() ? iterator.next() : null;
        }

        private Collection<Description> getAllDescendants(List<Description> steps) {
            List<Description> descendants = new ArrayList<>();
            for (Description child : steps) {
                descendants.add(child);
                descendants.addAll(getAllDescendants(child.getChildren()));
            }
            return descendants;
        }

        private List<Description> getStoryChildren() {
            return currentStoryDescription.getChildren();
        }
    }

    private enum StepStatus {
        STARTED, FINISHED
    }

    private enum ElementAction {
        DROP {
            @Override
            public <T> void compute(Collection<T> collection, T element) {
            }
        },
        TAKE {
            @Override
            public <T> void compute(Collection<T> collection, T element) {
                collection.add(element);
            }
        };

        public abstract <T> void compute(Collection<T> collection, T element);
    }
}
