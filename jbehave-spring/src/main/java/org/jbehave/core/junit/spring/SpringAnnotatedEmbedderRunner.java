package org.jbehave.core.junit.spring;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.spring.SpringAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runners.model.InitializationError;

public class SpringAnnotatedEmbedderRunner extends AnnotatedEmbedderRunner {

    public SpringAnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    protected AnnotationBuilder annotationBuilder() {
        return new SpringAnnotationBuilder(testClass());
    }

}
