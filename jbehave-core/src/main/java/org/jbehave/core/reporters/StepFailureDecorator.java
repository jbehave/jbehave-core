package org.jbehave.core.reporters;

import org.jbehave.core.failures.StepFailed;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * When a step fails, the {@link Throwable} that caused the failure is wrapped
 * in a {@link StepFailed} together with the step during which the failure
 * occurred. If such a failure occurs it will throw the {@link StepFailed}
 * after the story is finished.
 * </p>
 * 
 * @see StepFailed
 */
public class StepFailureDecorator implements StoryReporter {

	private final StoryReporter delegate;
	private StepFailed failure;

    public StepFailureDecorator(StoryReporter delegate) {
		this.delegate = delegate;
	}

    public void afterScenario() {
		delegate.afterScenario();
	}

	public void afterStory(boolean givenStory) {
		delegate.afterStory(givenStory);
		if (failure != null) {
			throw failure;
		}
	}

    public void beforeScenario(String scenarioTitle) {
		delegate.beforeScenario(scenarioTitle);
	}

    public void scenarioMeta(Meta meta) {
        delegate.scenarioMeta(meta);
    }

    public void beforeStory(Story story, boolean givenStory) {
        failure = null;
        delegate.beforeStory(story, givenStory);
    }

	public void failed(String step, Throwable cause) {
		failure = new StepFailed(step, cause);
		delegate.failed(step, failure);
	}

    public void failedOutcomes(String step, OutcomesTable table) {
		failure = new StepFailed(step, table);
    	delegate.failedOutcomes(step, table);
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

	public void givenStories(List<String> storyPaths) {
		delegate.givenStories(storyPaths);
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

	public void dryRun() {
		delegate.dryRun();
	}

    public void scenarioNotAllowed(Scenario scenario, String filter) {
        delegate.scenarioNotAllowed(scenario, filter);
    }

    public void storyNotAllowed(Story story, String filter) {
        delegate.storyNotAllowed(story, filter);
    }
    
}
