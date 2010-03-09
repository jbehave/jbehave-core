package org.jbehave.scenario.reporters;

import java.util.List;
import java.util.Map;

import org.jbehave.scenario.definition.Blurb;
import org.jbehave.scenario.definition.ExamplesTable;
import org.jbehave.scenario.definition.StoryDefinition;

/**
 * Allows the runner to report the state of running scenarios
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public interface ScenarioReporter {

    void beforeStory(StoryDefinition story, boolean embeddedStory);

    /**
     * @deprecated Use beforeStory(StoryDefinition, boolean)
     */
    void beforeStory(Blurb blurb);

    void afterStory(boolean embeddedStory);
    
    /**
     * @deprecated Use afterStory(boolean)
     */
    void afterStory();
    
    void beforeScenario(String title);
    
    void afterScenario();
    
	void givenScenarios(List<String> givenScenarios);

	/**
	 * @deprecated Use beforeExamples(List<String>, ExamplesTable)
	 */
	void examplesTable(ExamplesTable table);

	void beforeExamples(List<String> steps, ExamplesTable table);

    /**
     * @deprecated Use example(Map<String,String>)
     */	
	void examplesTableRow(Map<String, String> tableRow);

	void example(Map<String, String> tableRow);

    void afterExamples();

    void successful(String step);

    void ignorable(String step);

    void pending(String step);

    void notPerformed(String step);

    void failed(String step, Throwable e);

}
