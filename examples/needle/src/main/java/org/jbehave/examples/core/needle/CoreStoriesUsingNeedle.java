package org.jbehave.examples.core.needle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.needle.NeedleStepsFactory;
import org.jbehave.examples.core.CoreStories;
import org.jbehave.examples.core.needle.provider.TraderServiceInjectionProvider;
import org.jbehave.examples.core.needle.steps.NeedleTraderSteps;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

/**
 * Run core stories using NeedleStepsFactory. The textual trader stories are
 * exactly the same ones found in the jbehave-core-example. Here we are only
 * concerned with using the container to compose the steps instances.
 */
public class CoreStoriesUsingNeedle extends CoreStories {

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
    return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
  }

}
