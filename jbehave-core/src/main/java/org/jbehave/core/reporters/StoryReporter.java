package org.jbehave.core.reporters;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Story;

import java.util.List;
import java.util.Map;

/**
 * Allows the runner to report the state of running stories
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public interface StoryReporter {

    void beforeStory(Story story, boolean givenStory);

    void afterStory(boolean givenStory);

    void beforeScenario(String scenarioTitle);
    
    void afterScenario();
    
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
