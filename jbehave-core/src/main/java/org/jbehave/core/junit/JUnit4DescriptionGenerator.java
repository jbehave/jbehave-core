package org.jbehave.core.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.AllStepCandidates;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.ExamplePerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.BeforeOrAfterStep;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepType;
import org.junit.runner.Description;

public class JUnit4DescriptionGenerator {

    private final TextManipulator textManipulator = new TextManipulator();

    private final Configuration configuration;
    private final AllStepCandidates allStepCandidates;
    private String previousNonAndStep;
    private int testCases;

    private final JUnit4Test junitTestMeta;

    public JUnit4DescriptionGenerator(AllStepCandidates allStepCandidates, Configuration configuration) {
        this.configuration = configuration;
        this.allStepCandidates = allStepCandidates;
        this.junitTestMeta = new JUnit4Test() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return JUnit4Test.class;
            }
        };
    }

    public List<Description> createDescriptionsFrom(PerformableTree performableTree) {
        List<Description> storyDescriptions = new ArrayList<>();
        for (PerformableStory performableStory : performableTree.getRoot().getStories()) {
            if (!performableStory.isExcluded()) {
                Story story = performableStory.getStory();
                Lifecycle lifecycle = story.getLifecycle();
                Description storyDescription = createDescriptionForStory(story);
                addBeforeOrAfterStep(allStepCandidates.getBeforeStorySteps(false), storyDescription,
                        "@BeforeStory: ");
                addSteps(storyDescription, lifecycle.getBeforeSteps(Scope.STORY));
                if (story.hasGivenStories()) {
                    insertGivenStories(story.getGivenStories(), storyDescription);
                }
                List<PerformableScenario> scenarios = performableStory.getScenarios();
                for (Description scenarioDescription : getScenarioDescriptions(lifecycle, scenarios)) {
                    storyDescription.addChild(scenarioDescription);
                }
                addSteps(storyDescription, lifecycle.getAfterSteps(Scope.STORY, Outcome.ANY));
                addBeforeOrAfterStep(allStepCandidates.getAfterStorySteps(false), storyDescription,
                        "@AfterStory: ");
                storyDescriptions.add(storyDescription);
            }
        }
        return storyDescriptions;
    }

    public Description createDescriptionsFrom(Lifecycle lifecycle, PerformableScenario performableScenario) {
        Scenario scenario = performableScenario.getScenario();
        Description scenarioDescription = createDescriptionForScenario(scenario);
        if (performableScenario.hasExamples() && !scenario.getGivenStories().requireParameters()) {
            insertDescriptionForExamples(lifecycle, performableScenario, scenarioDescription);
        } else {
            addScenarioSteps(lifecycle, ScenarioType.NORMAL, scenario, scenarioDescription);
        }
        return scenarioDescription;
    }

    private void addScenarioSteps(Lifecycle lifecycle, ScenarioType scenarioType, Scenario scenario,
            Description scenarioDescription) {
        addBeforeOrAfterScenarioStep(allStepCandidates::getBeforeScenarioSteps, scenarioType, scenarioDescription,
                "@BeforeScenario: ");
        addSteps(scenarioDescription, lifecycle.getBeforeSteps(Scope.SCENARIO));
        if (scenario.hasGivenStories()) {
            insertGivenStories(scenario.getGivenStories(), scenarioDescription);
        }
        addScenarioSteps(lifecycle, scenarioDescription, scenario);
        addSteps(scenarioDescription, lifecycle.getAfterSteps(Scope.SCENARIO, Outcome.ANY));
        addBeforeOrAfterScenarioStep(allStepCandidates::getAfterScenarioSteps, scenarioType, scenarioDescription,
                "@AfterScenario: ");
    }

    private void addScenarioSteps(Lifecycle lifecycle, Description scenarioDescription, Scenario scenario) {
        List<String> beforeSteps = lifecycle.getBeforeSteps(Scope.STEP);
        List<String> afterSteps = lifecycle.getAfterSteps(Scope.STEP);
        previousNonAndStep = null;
        String tempPreviousNonAndStep = null;
        for (String scenarioStep : scenario.getSteps()) {
            addSteps(scenarioDescription, beforeSteps);
            previousNonAndStep = tempPreviousNonAndStep;
            addStep(scenarioDescription, scenarioStep);
            tempPreviousNonAndStep = previousNonAndStep;
            addSteps(scenarioDescription, afterSteps);
        }
    }

    private void addBeforeOrAfterScenarioStep(Function<ScenarioType, List<BeforeOrAfterStep>> stepsProvider,
            ScenarioType scenarioType, Description description, String stepName) {
        List<BeforeOrAfterStep> beforeOrAfterSteps = new ArrayList<>();
        beforeOrAfterSteps.addAll(stepsProvider.apply(scenarioType));
        beforeOrAfterSteps.addAll(stepsProvider.apply(ScenarioType.ANY));
        addBeforeOrAfterStep(beforeOrAfterSteps, description, stepName);
    }

    private void addBeforeOrAfterStep(List<BeforeOrAfterStep> beforeOrAfterSteps, Description description,
            String stepPrefix) {
        beforeOrAfterSteps.forEach(steps ->
        {
            testCases++;
            Method method = steps.getMethod();
            String stepName = uniquify(stepPrefix + steps.getMethod().getName());
            Description testDescription = Description.createTestDescription(method.getDeclaringClass(), stepName,
                    junitTestMeta);
            description.addChild(testDescription);
        });
    }

    public String uniquify(String string) {
        return textManipulator.uniquify(string);
    }

    public int getTestCases() {
        return testCases;
    }

    private void insertGivenStories(GivenStories givenStories, Description parentDescription) {
        for (String path : givenStories.getPaths()) {
            addGivenStory(parentDescription, path);
        }
    }

    private void addGivenStory(Description parentDescription, String path) {
        parentDescription.addChild(Description.createSuiteDescription(uniquify(getFilename(path)), junitTestMeta));
        testCases++;
    }

    private String getFilename(String path) {
        return path.substring(path.lastIndexOf('/') + 1).split("#")[0];
    }

    private void insertDescriptionForExamples(Lifecycle lifecycle, PerformableScenario performableScenario,
            Description scenarioDescription) {
        Scenario scenario = performableScenario.getScenario();
        for (ExamplePerformableScenario examplePerformableScenario : performableScenario.getExamples()) {
            Description exampleRowDescription = Description.createSuiteDescription(
                    configuration.keywords().examplesTableRow() + " " + examplePerformableScenario.getParameters());
            scenarioDescription.addChild(exampleRowDescription);
            addScenarioSteps(lifecycle, ScenarioType.EXAMPLE, scenario, exampleRowDescription);
        }
    }

    private void addSteps(Description description, List<String> steps) {
        previousNonAndStep = null;
        steps.forEach(step -> addStep(description, step));
    }

    private void addStep(Description description, String step) {
        String stringStepOneLine = stripLinebreaks(step);
        StepCandidate matchingStep = findMatchingStep(step);
        if (matchingStep == null) {
            addNonExistingStep(description, stringStepOneLine, step);
        } else {
            addExistingStep(description, stringStepOneLine, matchingStep);
        }
    }

    private void addExistingStep(Description description, String stringStepOneLine, StepCandidate matchingStep) {
        if (matchingStep.isComposite()) {
            addCompositeSteps(description, stringStepOneLine, matchingStep);
        } else {
            addRegularStep(description, stringStepOneLine, matchingStep);
        }
    }

    private void addNonExistingStep(Description description, String stringStepOneLine, String stepAsString) {
        Keywords keywords = configuration.keywords();
        if (keywords.isIgnorableStep(stepAsString)) {
            if (isStep(keywords.stepWithoutStartingWord(stepAsString, StepType.IGNORABLE))) {
                addIgnorableStep(description, stringStepOneLine);
            }
        } else {
            addPendingStep(description, stringStepOneLine);
        }
    }

    private boolean isStep(String stepAsString) {
        Keywords keywords = configuration.keywords();
        for (String stepStartingWord : keywords.startingWordsByType().values()) {
            if (keywords.stepStartsWithWord(stepAsString, stepStartingWord)) {
                return true;
            }
        }
        return false;
    }

    private void addIgnorableStep(Description description, String stringStep) {
        testCases++;
        description.addChild(Description.createSuiteDescription(stringStep));
    }

    private void addPendingStep(Description description, String stringStep) {
        testCases++;
        description.addChild(Description.createSuiteDescription(uniquify("[PENDING] " + stringStep)));
    }

    private void addRegularStep(Description description, String stringStep, StepCandidate step) {
        testCases++;
        // JUnit and the Eclipse JUnit view needs to be touched/fixed in order to make the JUnit view jump to the
        // corresponding test method accordingly. For now we have to live, that we end up in the correct class.
        description.addChild(Description.createTestDescription(step.getStepsType(), uniquify(stringStep),
                junitTestMeta));
    }

    private void addCompositeSteps(Description description, String stringStep, StepCandidate step) {
        Description testDescription = Description.createSuiteDescription(uniquify(stringStep), junitTestMeta);
        addSteps(testDescription, Arrays.asList(step.composedSteps()));
        description.addChild(testDescription);
    }

    private List<Description> getScenarioDescriptions(Lifecycle lifecycle,
            List<PerformableScenario> performableScenarios) {
        List<Description> scenarioDescriptions = new ArrayList<>();
        for (PerformableScenario scenario : performableScenarios) {
            if (!scenario.isExcluded()) {
                scenarioDescriptions.add(createDescriptionsFrom(lifecycle, scenario));
            }
        }
        return scenarioDescriptions;
    }

    private StepCandidate findMatchingStep(String stringStep) {
        for (StepCandidate step : allStepCandidates.getRegularSteps()) {
            if (step.matches(stringStep, previousNonAndStep)) {
                if (step.getStepType() != StepType.AND) {
                    previousNonAndStep = step.getStartingWord() + " ";
                }
                return step;
            }
        }
        return null;
    }

    private String stripLinebreaks(String stringStep) {
        if (stringStep.indexOf('\n') != -1) {
            return stringStep.substring(0, stringStep.indexOf('\n'));
        }
        return stringStep;
    }

    private Description createDescriptionForStory(Story story) {
        return Description.createSuiteDescription(uniquify(story.getName()));
    }

    private Description createDescriptionForScenario(Scenario scenario) {
        return Description.createSuiteDescription(
                configuration.keywords().scenario() + " " + uniquify(scenario.getTitle()));
    }

    public @interface JUnit4Test {
    }
}
