package org.jbehave.core.junit.guice;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.guice.GuiceAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedder;
import org.junit.runners.model.InitializationError;

public class GuiceAnnotatedEmbedder extends AnnotatedEmbedder {

    public GuiceAnnotatedEmbedder(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    protected AnnotationBuilder annotationBuilder() {
        return new GuiceAnnotationBuilder(testClass());
    }

}
