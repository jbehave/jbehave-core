package org.jbehave.examples.core;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.CompositeSteps;
import org.jbehave.examples.core.steps.FailingBeforeAfterSteps;
import org.jbehave.examples.core.steps.MetaParametrisationSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;
import org.jbehave.examples.core.steps.TraderSteps;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

public class FailingBeforeAfterCoreStories extends CoreStories {

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new TraderSteps(new TradingService()), new AndSteps(), new MetaParametrisationSteps(),
                new CalendarSteps(), new PriorityMatchingSteps(), new PendingSteps(), new SandpitSteps(),
                new SearchSteps(), new BeforeAfterSteps(), new CompositeSteps(), new FailingBeforeAfterSteps());
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/failing_before*.story", "");
                
    }
}