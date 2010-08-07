package org.jbehave.core.junit.pico;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.pico.PicoAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedEmbedderRunner that uses {@link PicoAnnotationBuilder}.
 */
public class PicoAnnotatedEmbedderRunner extends AnnotatedEmbedderRunner {
    
    private PicoAnnotationBuilder annotationBuilder;

    public PicoAnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        annotationBuilder = new PicoAnnotationBuilder(testClass());
    }

    public AnnotationBuilder annotationBuilder() {
        return annotationBuilder;
    }

}
