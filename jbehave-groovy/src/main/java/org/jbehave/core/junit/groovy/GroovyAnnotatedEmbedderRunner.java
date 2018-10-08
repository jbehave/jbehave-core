package org.jbehave.core.junit.groovy;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.groovy.GroovyAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedEmbedderRunner that uses {@link GroovyAnnotationBuilder}.
 */
public class GroovyAnnotatedEmbedderRunner extends AnnotatedEmbedderRunner {

    private GroovyAnnotationBuilder annotationBuilder;
    
    public GroovyAnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        annotationBuilder = new GroovyAnnotationBuilder(testClass());
    }

    @Override
    public AnnotationBuilder annotationBuilder() {
        return annotationBuilder;
    }

}
