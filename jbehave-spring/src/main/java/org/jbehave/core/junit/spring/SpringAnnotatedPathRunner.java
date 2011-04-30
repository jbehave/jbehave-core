package org.jbehave.core.junit.spring;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.spring.SpringAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedPathRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedPathRunner that uses {@link SpringAnnotationBuilder}.
 */
public class SpringAnnotatedPathRunner extends AnnotatedPathRunner {

    public SpringAnnotatedPathRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public AnnotationBuilder annotationBuilder() {
        return new SpringAnnotationBuilder(testClass());
    }

}
