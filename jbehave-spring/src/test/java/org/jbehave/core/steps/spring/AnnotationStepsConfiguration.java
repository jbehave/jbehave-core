package org.jbehave.core.steps.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnnotationStepsConfiguration {

    @Bean
    public SpringStepsFactoryBehaviour.FooSteps fooSteps() {
        return new SpringStepsFactoryBehaviour.FooSteps();
    }

    @Bean
    public SpringStepsFactoryBehaviour.AbstractSteps abstractSteps () {
        return new SpringStepsFactoryBehaviour.AbstractSteps() {

        };
    }
}
