package org.jbehave.examples.core.spring;

import org.jbehave.examples.core.service.TradingService;
import org.jbehave.examples.core.steps.AndSteps;
import org.jbehave.examples.core.steps.BeforeAfterSteps;
import org.jbehave.examples.core.steps.CalendarSteps;
import org.jbehave.examples.core.steps.CompositeSteps;
import org.jbehave.examples.core.steps.PendingSteps;
import org.jbehave.examples.core.steps.PriorityMatchingSteps;
import org.jbehave.examples.core.steps.SandpitSteps;
import org.jbehave.examples.core.steps.SearchSteps;
import org.jbehave.examples.core.steps.TraderSteps;
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
    public AndSteps andSteps() {
        return new AndSteps();
    }

    @Bean
    public CalendarSteps calendarSteps() {
        return new CalendarSteps();
    }

    @Bean
    public CompositeSteps compositeSteps() {
        return new CompositeSteps();
    }

    @Bean
    public PendingSteps pendingSteps() {
        return new PendingSteps();
    }

    @Bean
    public PriorityMatchingSteps priorityMatchingSteps() {
        return new PriorityMatchingSteps();
    }

    @Bean
    public SandpitSteps sandpitSteps() {
        return new SandpitSteps();
    }

    @Bean
    public SearchSteps searchSteps() {
        return new SearchSteps();
    }

    @Bean
    public TraderSteps traderSteps() {
        return new TraderSteps(new TradingService());
    }

}
