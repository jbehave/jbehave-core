package org.jbehave.core.failures;

/**
 * PendingStepStrategy allows to define how pending steps are handled. Two
 * standard strategies are provided:
 * <ul>
 * <li>{@link PassingUponPendingStep}: passes upon pending step</li>
 * <li>{@link FailingUponPendingStep}: fails upon pending step</li>
 * </ul>
 */
public interface PendingStepStrategy extends FailureStrategy {
    
}
