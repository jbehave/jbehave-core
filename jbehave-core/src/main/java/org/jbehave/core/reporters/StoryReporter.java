package org.jbehave.core.reporters;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * Allows the runner to report the state of running stories
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public interface StoryReporter {

    void storyNotAllowed(Story story, String filter);

    void storyCancelled(Story story, StoryDuration storyDuration);

    void beforeStory(Story story, boolean givenStory);

    void afterStory(boolean givenOrRestartingStory);

    void narrative(Narrative narrative);

    void lifecyle(Lifecycle lifecycle);

    void beforeStorySteps(Stage stage);

    void afterStorySteps(Stage stage);

    void beforeScenarioSteps(Stage stage);

    void afterScenarioSteps(Stage stage);

    void scenarioNotAllowed(Scenario scenario, String filter);

    void beforeScenario(Scenario scenario);

    /**
     * @deprecated use {@link #beforeScenario(Scenario)}
     *
     * @param scenarioTitle Scenario title
     */
    @Deprecated
    void beforeScenario(String scenarioTitle);

    /**
     * @deprecated use {@link #beforeScenario(Scenario)}
     *
     * @param meta Scenario meta
     */
    @Deprecated
    void scenarioMeta(Meta meta);

    void afterScenario();

    void beforeGivenStories();

    void givenStories(GivenStories givenStories);

    void givenStories(List<String> storyPaths);

    void afterGivenStories();

    void beforeExamples(List<String> steps, ExamplesTable table);

    /**
     * @deprecated use {@link #example(Map, int)}
     *
     * @param tableRow Example table row
     */
    @Deprecated
    void example(Map<String, String> tableRow);

    void example(Map<String, String> tableRow, int exampleIndex);

    void afterExamples();

    void beforeStep(String step);
    
    void successful(String step);

    void ignorable(String step);

    void comment(String step);

    void pending(String step);

    void notPerformed(String step);

    void failed(String step, Throwable cause);

    void failedOutcomes(String step, OutcomesTable table);

    void restarted(String step, Throwable cause);
    
    void restartedStory(Story story, Throwable cause);

    void dryRun();

    void pendingMethods(List<String> methods);
}
