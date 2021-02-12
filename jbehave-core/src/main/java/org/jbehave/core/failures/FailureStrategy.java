package org.jbehave.core.failures;

/**
 * FailureStrategy allows to define failure handling strategies. Two standard
 * strategies are provided:
 * <ul>
 * <li>{@link SilentlyAbsorbingFailure}: silently absorbs the failure</li>
 * <li>{@link RethrowingFailure}: rethrows the failure</li>
 * </ul>
 */
public interface FailureStrategy {

    void handleFailure(Throwable throwable) throws Throwable;

}
