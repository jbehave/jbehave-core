package org.jbehave.examples.core.needle;

import java.util.HashSet;
import java.util.Set;

import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.needle.NeedleStepsFactory;
import org.jbehave.examples.core.CoreStories;
import org.jbehave.examples.core.needle.steps.NeedleTraderSteps;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;
import org.needle4j.injection.InjectionProvider;
import org.needle4j.injection.InjectionProviders;

/**
 * Run core stories using NeedleStepsFactory. The textual stories are
 * exactly the same ones found in the jbehave-core-example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class CoreStoriesUsingNeedle extends CoreStories {

    @Override
    public InjectableStepsFactory stepsFactory() {
        final Class<?>[] steps = new Class<?>[] {
            NeedleTraderSteps.class,
            BeforeAfterSteps.class,
            AndSteps.class,
            CalendarSteps.class,
            PendingSteps.class,
            PriorityMatchingSteps.class,
            SandpitSteps.class,
            SearchSteps.class
        };

        final Set<InjectionProvider<?>> providers = new HashSet<>();
        providers.add(InjectionProviders.providerForInstance(new TradingService()));

        return new NeedleStepsFactory(configuration(), providers, steps);
    }

}
