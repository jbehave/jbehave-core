package org.jbehave.core.reporters;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.Timing;

/**
 * Allows the runner to report the state of running stories
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public interface StoryReporter {

    void beforeStoriesSteps(Stage stage);

    void afterStoriesSteps(Stage stage);

    void storyExcluded(Story story, String filter);

    void storyCancelled(Story story, StoryDuration storyDuration);

    void beforeStory(Story story, boolean givenStory);

    void afterStory(boolean givenOrRestartingStory);

    void beforeScenarios();

    void afterScenarios();

    void narrative(Narrative narrative);

    void lifecycle(Lifecycle lifecycle);

    void beforeStorySteps(Stage stage, Lifecycle.ExecutionType type);

    void afterStorySteps(Stage stage, Lifecycle.ExecutionType type);

    void beforeScenarioSteps(Stage stage, Lifecycle.ExecutionType type);

    void afterScenarioSteps(Stage stage, Lifecycle.ExecutionType type);

    void beforeComposedSteps();

    void afterComposedSteps();

    void scenarioExcluded(Scenario scenario, String filter);

    void beforeScenario(Scenario scenario);

    void afterScenario(Timing timing);

    void beforeGivenStories();

    void givenStories(GivenStories givenStories);

    void givenStories(List<String> storyPaths);

    void afterGivenStories();

    void beforeExamples(List<String> steps, ExamplesTable table);

    void example(Map<String, String> tableRow, int exampleIndex);

    void afterExamples();

    void beforeStep(Step step);
    
    void successful(String step);

    void ignorable(String step);

    void comment(String step);

    void pending(PendingStep step);

    /**
     * Report pending step
     * @param step string representation of pending step
     * @deprecated Use {@link #pending(PendingStep)}
     */
    @Deprecated
    void pending(String step);

    void notPerformed(String step);

    void failed(String step, Throwable cause);

    void failedOutcomes(String step, OutcomesTable table);

    void restarted(String step, Throwable cause);
    
    void restartedStory(Story story, Throwable cause);

    void dryRun();

    /**
     * Report list of pending methods
     * @param methods list of generated methods
     * @deprecated pendingMethod info is added as a part of each pending step {@link #pending(PendingStep)}
     */
    @Deprecated
    void pendingMethods(List<String> methods);
}
