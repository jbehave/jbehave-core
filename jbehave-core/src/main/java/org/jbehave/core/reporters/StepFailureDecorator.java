package org.jbehave.core.reporters;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.failures.StepFailed;
import org.jbehave.core.model.*;

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
public class StepFailureDecorator extends DelegatingStoryReporter {

    private UUIDExceptionWrapper failure;

    public StepFailureDecorator(StoryReporter delegate) {
        super(delegate);
    }

    @Override
    public void afterStory(boolean givenStory) {
        super.afterStory(givenStory);
        if (failure != null) {
            throw failure;
        }
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        failure = null;
        super.beforeStory(story, givenStory);
    }

    @Override
    public void failed(String step, Throwable cause) {
        failure = (UUIDExceptionWrapper) cause;
        super.failed(step, failure);
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        failure = new StepFailed(step, table);
        super.failedOutcomes(step, table);
    }
}
