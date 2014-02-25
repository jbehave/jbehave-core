package org.jbehave.examples.core.stories;

import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.core.CoreStory;
import org.jbehave.examples.core.steps.BankAccountSteps;

public class Lifecycle extends CoreStory {

	@Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new BankAccountSteps());
	}

}
