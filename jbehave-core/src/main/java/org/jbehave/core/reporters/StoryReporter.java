package org.jbehave.core.reporters;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Story;

/**
 * Allows the runner to report the state of running stories
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public interface StoryReporter {

    void beforeStory(Story story, boolean embeddedStory);

    void afterStory(boolean embeddedStory);

    void beforeScenario(String title);
    
    void afterScenario();
    
	void givenStories(List<String> storyPaths);

    void beforeExamples(List<String> steps, ExamplesTable table);

    void example(Map<String, String> tableRow);

    void afterExamples();

    void successful(String step);

    void ignorable(String step);

    void pending(String step);

    void notPerformed(String step);

    void failed(String step, Throwable e);

}
