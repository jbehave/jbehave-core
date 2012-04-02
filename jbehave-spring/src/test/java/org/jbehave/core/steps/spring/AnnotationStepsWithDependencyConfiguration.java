package org.jbehave.core.steps.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnnotationStepsWithDependencyConfiguration {

    @Bean
    public SpringStepsFactoryBehaviour.FooStepsWithDependency fooSteps() {
        return new SpringStepsFactoryBehaviour.FooStepsWithDependency(42);
    }
}
