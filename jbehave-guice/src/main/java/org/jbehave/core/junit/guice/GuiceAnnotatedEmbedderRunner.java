package org.jbehave.core.junit.guice;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.guice.GuiceAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedEmbedderRunner that uses {@link GuiceAnnotationBuilder}.
 */
public class GuiceAnnotatedEmbedderRunner extends AnnotatedEmbedderRunner {

    private GuiceAnnotationBuilder annotationBuilder;

    public GuiceAnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.annotationBuilder = new GuiceAnnotationBuilder(testClass());
    }

    public AnnotationBuilder annotationBuilder() {
        return annotationBuilder;
    }

}
