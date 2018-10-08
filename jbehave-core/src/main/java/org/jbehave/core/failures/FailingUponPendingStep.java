package org.jbehave.core.failures;

public final class FailingUponPendingStep implements PendingStepStrategy {

    @Override
    public void handleFailure(Throwable throwable) throws Throwable {
        throw throwable;
    }

}
