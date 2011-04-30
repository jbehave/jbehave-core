package org.jbehave.core.junit.guice;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.guice.GuiceAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedPathRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedPathRunner that uses {@link GuiceAnnotationBuilder}.
 */
public class GuiceAnnotatedPathRunner extends AnnotatedPathRunner {

    public GuiceAnnotatedPathRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public AnnotationBuilder annotationBuilder() {
        return new GuiceAnnotationBuilder(testClass());
    }

}
