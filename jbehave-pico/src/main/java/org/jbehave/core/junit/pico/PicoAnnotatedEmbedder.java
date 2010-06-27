package org.jbehave.core.junit.pico;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.pico.PicoAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedder;
import org.junit.runners.model.InitializationError;

public class PicoAnnotatedEmbedder extends AnnotatedEmbedder {

    public PicoAnnotatedEmbedder(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    protected AnnotationBuilder annotationBuilder() {
        return new PicoAnnotationBuilder(testClass());
    }

}
