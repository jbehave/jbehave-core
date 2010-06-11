package org.jbehave.core.failures;

public final class FailingUponPendingStep implements PendingStepStrategy {
	
	public void handleFailure(Throwable throwable) throws Throwable {
        throw throwable;
    }

}