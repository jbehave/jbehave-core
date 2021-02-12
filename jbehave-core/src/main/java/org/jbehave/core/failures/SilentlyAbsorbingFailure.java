package org.jbehave.core.failures;

public final class SilentlyAbsorbingFailure implements FailureStrategy {

    @Override
    public void handleFailure(Throwable throwable) {
        // do nothing
    }

}
