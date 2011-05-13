package org.jbehave.examples.trader.stories;

import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.TraderStory;
import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.AndSteps;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.CalendarSteps;
import org.jbehave.examples.trader.steps.CompositeSteps;
import org.jbehave.examples.trader.steps.MetaParametrisationSteps;
import org.jbehave.examples.trader.steps.PendingSteps;
import org.jbehave.examples.trader.steps.PriorityMatchingSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;
import org.jbehave.examples.trader.steps.SearchSteps;
import org.jbehave.examples.trader.steps.TraderSteps;

public class FailingBeforeStories extends TraderStory {

    @Override
    public Configuration configuration() {
        return super.configuration().useStoryControls(new StoryControls().doResetStateBeforeStory(false).doResetStateBeforeScenario(false));
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new TraderSteps(new TradingService()), new AndSteps(), new MetaParametrisationSteps(),
                new CalendarSteps(), new PriorityMatchingSteps(), new PendingSteps(), new SandpitSteps(),
                new SearchSteps(), new BeforeAfterSteps(), new CompositeSteps(), this);
    }

    @BeforeStories
    public void beforeStories(){
        throw new RuntimeException("Bum go the stories");
    }
}
