package org.jbehave.core.junit.pico;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.pico.PicoAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedPathRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedPathRunner that uses {@link PicoAnnotationBuilder}.
 */
public class PicoAnnotatedPathRunner extends AnnotatedPathRunner {

    public PicoAnnotatedPathRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public AnnotationBuilder annotationBuilder() {
        return new PicoAnnotationBuilder(testClass());
    }

}
