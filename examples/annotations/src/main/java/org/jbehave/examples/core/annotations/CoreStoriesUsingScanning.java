package org.jbehave.examples.core.annotations;

import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ScanningStepsFactory;
import org.jbehave.examples.core.CoreStories;

/**
 * <p>
 * Example of how multiple stories can be run via JUnit, finding steps 
 * via the {@link ScanningStepsFactory}.
 * </p>
 */
public class CoreStoriesUsingScanning extends CoreStories {

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new ScanningStepsFactory(configuration(), "org.jbehave.examples.core.steps").notMatchingNames(
                ".*Failing.*");
    }

}