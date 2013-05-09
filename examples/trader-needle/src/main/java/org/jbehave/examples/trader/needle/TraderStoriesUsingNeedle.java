package org.jbehave.examples.trader.needle;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.needle.NeedleStepsFactory;
import org.jbehave.examples.trader.TraderStories;
import org.jbehave.examples.trader.needle.provider.TraderServiceInjectionProvider;
import org.jbehave.examples.trader.steps.AndSteps;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.CalendarSteps;
import org.jbehave.examples.trader.steps.PendingSteps;
import org.jbehave.examples.trader.steps.PriorityMatchingSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;
import org.jbehave.examples.trader.steps.SearchSteps;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;

/**
 * Run trader stories using NeedleStepsFactory. The textual trader stories are
 * exactly the same ones found in the jbehave-trader-example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class TraderStoriesUsingNeedle extends TraderStories {

  @Override
  public InjectableStepsFactory stepsFactory() {
    final Class<?>[] steps = new Class<?>[] { NeedleTraderSteps.class, BeforeAfterSteps.class, AndSteps.class, CalendarSteps.class, PendingSteps.class,
        PriorityMatchingSteps.class, SandpitSteps.class, SearchSteps.class };

    final Set<InjectionProvider<?>> providers = new HashSet<InjectionProvider<?>>();
    providers.add(new TraderServiceInjectionProvider());

    return new NeedleStepsFactory(configuration(), providers, steps);
  }

  @Override
  protected List<String> storyPaths() {
    return new StoryFinder().findPaths(codeLocationFromPath("../trader/src/main/java"), "**/*.story", "");
  }

}
