package org.jbehave.core.junit.guice;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.guice.GuiceAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runners.model.InitializationError;

public class GuiceAnnotatedEmbedderRunner extends AnnotatedEmbedderRunner {

    private GuiceAnnotationBuilder guiceAnnotationBuilder;

    public GuiceAnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.guiceAnnotationBuilder = new GuiceAnnotationBuilder(testClass());
    }

    protected AnnotationBuilder annotationBuilder() {
        return guiceAnnotationBuilder;
    }

}
