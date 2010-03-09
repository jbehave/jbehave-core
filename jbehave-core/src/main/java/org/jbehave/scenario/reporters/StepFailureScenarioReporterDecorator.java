package org.jbehave.scenario.reporters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.scenario.definition.Blurb;
import org.jbehave.scenario.definition.ExamplesTable;
import org.jbehave.scenario.definition.StoryDefinition;
import org.jbehave.scenario.errors.StepFailure;

/**
 * <p>
 * When a step fails, the {@link Throwable} that caused the failure is wrapped
 * in a {@link StepFailure} together with the step during which the failure
 * occurred. If such a failure occurs it will throw the {@link StepFailure}
 * after the story is finished.
 * </p>
 * 
 * @see StepFailure
 */
public class StepFailureScenarioReporterDecorator implements ScenarioReporter {

	private final ScenarioReporter delegate;
	private StepFailure failure;

	public StepFailureScenarioReporterDecorator(ScenarioReporter delegate) {
		this.delegate = delegate;
	}

	public void afterScenario() {
		delegate.afterScenario();
	}

	public void afterStory(boolean embeddedStory) {
		delegate.afterStory(embeddedStory);
		if (failure != null) {
			throw failure;
		}
	}

    public void afterStory() {
        afterStory(false);
    }
	
	public void beforeScenario(String title) {
		delegate.beforeScenario(title);
	}

	public void beforeStory(Blurb blurb) {
	    beforeStory(new StoryDefinition(blurb), false);
	}

    public void beforeStory(StoryDefinition story, boolean embeddedStory) {
        failure = null;
        delegate.beforeStory(story, embeddedStory);
    }

	public void failed(String step, Throwable cause) {
		failure = new StepFailure(step, cause);
		delegate.failed(step, failure);
	}

    public void ignorable(String step) {
        delegate.ignorable(step);
    }
    
	public void notPerformed(String step) {
		delegate.notPerformed(step);
	}

	public void pending(String step) {
		delegate.pending(step);
	}

	public void successful(String step) {
		delegate.successful(step);
	}

	public void givenScenarios(List<String> givenScenarios) {
		delegate.givenScenarios(givenScenarios);		
	}

	public void beforeExamples(List<String> steps, ExamplesTable table) {
		delegate.beforeExamples(steps, table);
	}

	public void example(Map<String, String> tableRow) {
		delegate.example(tableRow);
	}

    public void afterExamples() {
        delegate.afterExamples();        
    }

    public void examplesTable(ExamplesTable table) {
        beforeExamples(new ArrayList<String>(), table);
    }

    public void examplesTableRow(Map<String, String> tableRow) {
        example(tableRow);
    }

}
