package org.jbehave.core.failures;

public final class RethrowingFailure implements FailureStrategy {

	@Override
    public void handleFailure(Throwable throwable) throws Throwable {
	    if ( throwable instanceof UUIDExceptionWrapper ){
	        throw ((UUIDExceptionWrapper)throwable).getCause();
	    }
		throw throwable;
	}

}
