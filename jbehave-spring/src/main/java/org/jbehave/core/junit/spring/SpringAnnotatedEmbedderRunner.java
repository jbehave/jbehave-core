package org.jbehave.core.junit.spring;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.spring.SpringAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runners.model.InitializationError;

public class SpringAnnotatedEmbedderRunner extends AnnotatedEmbedderRunner {

    private SpringAnnotationBuilder annotationBuilder;
    
    public SpringAnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        annotationBuilder = new SpringAnnotationBuilder(testClass());
    }

    protected AnnotationBuilder annotationBuilder() {
        return annotationBuilder;
    }

}
