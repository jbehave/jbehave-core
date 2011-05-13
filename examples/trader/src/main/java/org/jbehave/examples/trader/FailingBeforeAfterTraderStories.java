package org.jbehave.examples.trader;

import java.util.List;

import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.AndSteps;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.CalendarSteps;
import org.jbehave.examples.trader.steps.CompositeSteps;
import org.jbehave.examples.trader.steps.FailingBeforeAfterSteps;
import org.jbehave.examples.trader.steps.MetaParametrisationSteps;
import org.jbehave.examples.trader.steps.PendingSteps;
import org.jbehave.examples.trader.steps.PriorityMatchingSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;
import org.jbehave.examples.trader.steps.SearchSteps;
import org.jbehave.examples.trader.steps.TraderSteps;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

public class FailingBeforeAfterTraderStories extends TraderStories {

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