package org.jbehave.core.failures;

public final class RethrowingFailure implements FailureStrategy {

	public void handleFailure(Throwable throwable) throws Throwable {
		throw throwable;
	}

}