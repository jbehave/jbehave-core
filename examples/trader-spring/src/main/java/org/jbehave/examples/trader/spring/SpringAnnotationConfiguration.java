package org.jbehave.examples.trader.spring;

import org.jbehave.examples.trader.service.TradingService;
import org.jbehave.examples.trader.steps.AndSteps;
import org.jbehave.examples.trader.steps.BeforeAfterSteps;
import org.jbehave.examples.trader.steps.CalendarSteps;
import org.jbehave.examples.trader.steps.CompositeSteps;
import org.jbehave.examples.trader.steps.PendingSteps;
import org.jbehave.examples.trader.steps.PriorityMatchingSteps;
import org.jbehave.examples.trader.steps.SandpitSteps;
import org.jbehave.examples.trader.steps.SearchSteps;
import org.jbehave.examples.trader.steps.TraderSteps;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A Spring 3.x annotation-based configuration class
 */
@Configuration
public class SpringAnnotationConfiguration {

    @Bean
    public BeforeAfterSteps beforeAfterSteps() {
        return new BeforeAfterSteps();
    }

    @Bean
    public AndSteps andSteps () {
        return new AndSteps();
    }

    @Bean
    public CalendarSteps calendarSteps () {
        return new CalendarSteps();
    }

    @Bean
    public CompositeSteps compositeSteps () {
        return new CompositeSteps();
    }

    @Bean
    public PendingSteps pendingSteps () {
        return new PendingSteps();
    }

    @Bean
    public PriorityMatchingSteps priorityMatchingSteps () {
        return new PriorityMatchingSteps();
    }

    @Bean
    public SandpitSteps sandpitSteps () {
        return new SandpitSteps();
    }

    @Bean
    public SearchSteps searchSteps () {
        return new SearchSteps();
    }

    @Bean
    public TraderSteps traderSteps () {
        return new TraderSteps(new TradingService());
    }

}
