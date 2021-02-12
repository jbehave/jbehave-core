package org.jbehave.core.junit.needle;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.needle.NeedleAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedEmbedderRunner that uses {@link NeedleAnnotationBuilder}.
 */
public class NeedleAnnotatedEmbedderRunner extends AnnotatedEmbedderRunner {

    private NeedleAnnotationBuilder annotationBuilder;

    public NeedleAnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.annotationBuilder = new NeedleAnnotationBuilder(testClass());
    }

    @Override
    public AnnotationBuilder annotationBuilder() {
        return annotationBuilder;
    }

}
