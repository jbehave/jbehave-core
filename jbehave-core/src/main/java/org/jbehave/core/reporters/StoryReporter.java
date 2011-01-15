package org.jbehave.core.reporters;

import org.jbehave.core.model.*;

import java.util.List;
import java.util.Map;

/**
 * Allows the runner to report the state of running stories
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public interface StoryReporter {

    void storyNotAllowed(Story story, String filter);

    void beforeStory(Story story, boolean givenStory);

    void afterStory(boolean givenStory);

    void scenarioNotAllowed(Scenario scenario, String filter);

    void beforeScenario(String scenarioTitle);
    
    void scenarioMeta(Meta meta);

    void afterScenario();

    void givenStories(GivenStories givenStories);

	void givenStories(List<String> storyPaths);

    void beforeExamples(List<String> steps, ExamplesTable table);

    void example(Map<String, String> tableRow);

    void afterExamples();

    void successful(String step);

    void ignorable(String step);

    void pending(String step);

    void notPerformed(String step);

    void failed(String step, Throwable cause);

	void failedOutcomes(String step, OutcomesTable table);

	void dryRun();

}
